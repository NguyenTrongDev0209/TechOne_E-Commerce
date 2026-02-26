package com.techone.controller.api;

import com.techone.model.*;
import com.techone.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final AddressRepository addressRepository;
    private final PayOS payOS;

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> payload, HttpSession session,
            HttpServletRequest request) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để đặt hàng");
        }

        String paymentMethod = (String) payload.get("paymentMethod");
        String note = (String) payload.get("note");
        Integer addressId = payload.get("addressId") != null ? Integer.parseInt(payload.get("addressId").toString())
                : null;

        if (addressId == null) {
            return ResponseEntity.badRequest().body("Vui lòng chọn địa chỉ giao hàng");
        }

        Optional<Address> addressOpt = addressRepository.findById(addressId);
        if (addressOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Địa chỉ không tồn tại");
        }

        Optional<Cart> cartOpt = cartRepository.findByAccount(user);
        if (cartOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }

        List<CartItem> cartItems = cartItemRepository.findByCart(cartOpt.get());
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }

        // 1. Create Order
        Order order = new Order();
        order.setCreateAt(LocalDateTime.now());
        order.setStatus(0); // 0: Pending/Processing
        order.setNote(note);
        order.setAccount(user);
        order = orderRepository.save(order);

        // 1.5 Create Shipment
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setAddress(addressOpt.get());
        shipment.setStatus(0); // 0: Pending/Processing
        shipment.setCreateAt(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // 2. Create OrderDetails
        long totalAmount = 0;
        List<PaymentLinkItem> payOSItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setVariant(item.getVariant());
            detail.setQuantity(item.getQuantity());

            double pricePerItem = item.getVariant().getPrice() * (1 - item.getVariant().getDiscount() / 100.0);
            detail.setUnitPrice(pricePerItem);
            orderDetailRepository.save(detail);

            totalAmount += (long) (pricePerItem * item.getQuantity());

            payOSItems.add(PaymentLinkItem.builder()
                    .name(item.getVariant().getProduct().getName())
                    .quantity(item.getQuantity())
                    .price((long) pricePerItem)
                    .build());
        }

        // 3. Handle Payment
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());

        if ("QR".equals(paymentMethod)) {
            try {
                String baseUrl = getBaseUrl(request);
                String returnUrl = baseUrl + "/payment/success";
                String cancelUrl = baseUrl + "/payment/cancel";

                // PayOS orderCode must be a number. We use order.getId()
                // note: PayOS orderCode should be unique and ideally long.
                long orderCode = order.getId();

                CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount(totalAmount)
                        .description("Thanh toan don hang #" + order.getId())
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
                        .items(payOSItems)
                        .build();

                CreatePaymentLinkResponse payosResponse = payOS.paymentRequests().create(paymentData);

                response.put("paymentMethod", "PAYOS");
                response.put("checkoutUrl", payosResponse.getCheckoutUrl());
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Lỗi tạo liên kết thanh toán PayOS: " + e.getMessage());
            }
        } else {
            response.put("paymentMethod", "COD");
            response.put("redirectUrl", "/checkout/order-success");
        }

        // 4. Clear Cart after successful order placement (or wait for payment?)
        // In this simple flow, let's clear it now for COD, or let the webhook clear it
        // for PayOS?
        // Usually, we clear it once the order is "placed".
        if (!"QR".equals(paymentMethod)) {
            cartItemRepository.deleteAll(cartItems);
            session.setAttribute("cartCount", 0);
        }

        return ResponseEntity.ok(response);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }
}
