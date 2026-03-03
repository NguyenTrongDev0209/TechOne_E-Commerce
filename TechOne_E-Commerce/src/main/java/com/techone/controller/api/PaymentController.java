package com.techone.controller.api;

import com.techone.model.Order;
import com.techone.model.Transaction;
import com.techone.model.VoucherItem;
import com.techone.repository.OrderRepository;
import com.techone.repository.TransactionRepository;
import com.techone.repository.VoucherItemRepository;
import com.techone.service.PaymentService;
import com.techone.service.VNPayService;
import com.techone.service.OrderEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PayOS payOS;
    private final VNPayService vnPayService;
    private final OrderEmailService orderEmailService;
    private final TransactionRepository transactionRepository;
    private final VoucherItemRepository voucherItemRepository;

    @PostMapping("/payos_transfer_handler")
    public Object payosTransferHandler(@RequestBody Object body) {
        try {
            paymentService.processWebhook(body);
            return Map.of("success", true, "message", "Webhook delivered");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/check-status/{orderId}")
    public Object getPaymentStatus(@PathVariable("orderId") int orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // 1. If already marked as PAID in our DB, return success immediately
            if (order.getStatus() != null && order.getStatus() == 2) {
                return java.util.Map.of("status", "PAID");
            }

            // 2. Poll PayOS API directly for real-time status (matching demo behavior)
            if (order.getOrderCode() != null) {
                try {
                    vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests()
                            .get(order.getOrderCode());
                    String payosStatus = paymentLink.getStatus().toString();

                    // 3. If PayOS says PAID, sync our DB and return PAID
                    if ("PAID".equals(payosStatus)) {
                        paymentService.verifyAndUpdateOrder(order.getOrderCode());
                        return Map.of("status", "PAID");
                    }

                    // 4. Return whatever status PayOS reports (PENDING, CANCELLED, etc.)
                    return Map.of("status", payosStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback to local status if API fails
                }
            }
            return Map.of("status",
                    (order.getStatus() != null && order.getStatus() == 1) ? "PAID" : "PENDING");
        }
        return Map.of("status", "NOT_FOUND");
    }

    @GetMapping("/vnpay-return")
    public void handleVNPayReturn(jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        int paymentStatus = vnPayService.orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");

        try {
            if (paymentStatus == 1) {
                // Success
                String orderIdStr = orderInfo.substring(orderInfo.lastIndexOf("#") + 1);
                int orderId = Integer.parseInt(orderIdStr);
                Optional<Order> orderOpt = orderRepository.findById(orderId);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    if (order.getStatus() != 2) {
                        order.setStatus(2);
                        orderRepository.save(order);

                        // Mark voucher là đã sử dụng khi VNPAY xác nhận thanh toán thành công
                        if (order.getAppliedVoucher() != null && order.getAppliedVoucher().getStatus() == 0) {
                            VoucherItem voucher = order.getAppliedVoucher();
                            voucher.setStatus(1);
                            voucherItemRepository.save(voucher);
                        }

                        // Save Transaction
                        Transaction transaction = new Transaction();
                        transaction.setOrder(order);
                        transaction.setAmount(Double.parseDouble(request.getParameter("vnp_Amount")) / 100.0);
                        transaction.setPaymentMethod("VNPAY");
                        transaction.setTransactionType("PAYMENT");
                        transaction.setStatus(1); // Success
                        transaction.setOrderCode(Long.parseLong(request.getParameter("vnp_TxnRef")));
                        transaction.setReference(request.getParameter("vnp_TransactionNo"));
                        transaction.setLog("VNPAY Success: " + vnp_ResponseCode);
                        transaction.setCreateAt(LocalDateTime.now());
                        transactionRepository.save(transaction);

                        // Send Email
                        try {
                            orderEmailService.sendOrderInvoice(order);
                        } catch (Exception e) {
                            System.err.println("DEBUG: Failed to send VNPAY invoice email: " + e.getMessage());
                        }
                    }
                    response.sendRedirect("/payment/success");
                    return;
                }
            } else if (paymentStatus == -1) {
                System.err.println("SECURITY WARNING: VNPAY Invalid Signature detected for OrderInfo: " + orderInfo);
            } else {
                System.err.println("VNPAY Payment failed with ResponseCode: " + vnp_ResponseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("/payment/cancel");
    }
}
