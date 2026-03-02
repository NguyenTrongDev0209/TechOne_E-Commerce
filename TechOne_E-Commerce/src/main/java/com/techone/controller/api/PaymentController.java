package com.techone.controller.api;

import com.techone.model.Order;
import com.techone.repository.OrderRepository;
import com.techone.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;

import java.util.Optional;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PayOS payOS;

    @PostMapping("/payos_transfer_handler")
    public Object payosTransferHandler(@RequestBody Object body) {
        try {
            paymentService.processWebhook(body);
            return java.util.Map.of("success", true, "message", "Webhook delivered");
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Map.of("success", false, "message", e.getMessage());
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
                        return java.util.Map.of("status", "PAID");
                    }

                    // 4. Return whatever status PayOS reports (PENDING, CANCELLED, etc.)
                    return java.util.Map.of("status", payosStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback to local status if API fails
                }
            }
            return java.util.Map.of("status",
                    (order.getStatus() != null && order.getStatus() == 1) ? "PAID" : "PENDING");
        }
        return java.util.Map.of("status", "NOT_FOUND");
    }
}
