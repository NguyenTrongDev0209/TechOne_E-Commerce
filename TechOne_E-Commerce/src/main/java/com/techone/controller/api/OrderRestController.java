package com.techone.controller.api;

import com.techone.model.*;
import com.techone.repository.*;
import com.techone.service.ShippingFeeService;
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
    private final VoucherItemRepository voucherItemRepository;
    private final ShippingFeeService shippingFeeService;
    private final VariantRepository variantRepository;
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
        Integer addressId = payload.get("addressId") != null && !payload.get("addressId").toString().isEmpty()
                ? Integer.parseInt(payload.get("addressId").toString())
                : null;
        Integer voucherId = payload.get("voucherId") != null && !payload.get("voucherId").toString().isEmpty()
                ? Integer.parseInt(payload.get("voucherId").toString())
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

        // 0.5 Check Stock
        for (CartItem item : cartItems) {
            Variant variant = item.getVariant();
            if (variant.getStock() == null || variant.getStock() < item.getQuantity()) {
                return ResponseEntity.badRequest()
                        .body("Sản phẩm '" + variant.getProduct().getName() + " [" + variant.getVariantName()
                                + "]' đã hết hàng hoặc không đủ số lượng (Còn lại: "
                                + (variant.getStock() != null ? variant.getStock() : 0) + ")");
            }
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

        // 2. Calculate Shipping Fee
        Integer shippingFee = 0;
        try {
            shippingFee = shippingFeeService.calculateShippingFee(null,
                    addressOpt.get().getWard().getDistrict().getId(),
                    addressOpt.get().getWard().getCode());
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to 0 if calculation fails, or we could return error
        }
        order.setShippingFee(shippingFee.doubleValue());

        // 3. Create OrderDetails & Calculate Total
        long productTotal = 0;
        List<PaymentLinkItem> payOSItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setVariant(item.getVariant());
            detail.setQuantity(item.getQuantity());

            double pricePerItem = item.getVariant().getPrice() * (1 - item.getVariant().getDiscount() / 100.0);
            detail.setUnitPrice(pricePerItem);
            orderDetailRepository.save(detail);

            // 3.1 Reduce Stock
            Variant variant = item.getVariant();
            variant.setStock(variant.getStock() - item.getQuantity());
            variantRepository.save(variant);

            productTotal += (long) (pricePerItem * item.getQuantity());

            payOSItems.add(PaymentLinkItem.builder()
                    .name(item.getVariant().getProduct().getName())
                    .quantity(item.getQuantity())
                    .price((long) pricePerItem)
                    .build());
        }

        long finalTotalAmount = productTotal + shippingFee;

        // 3.5 Apply Voucher
        double voucherDiscount = 0;
        if (voucherId != null) {
            Optional<VoucherItem> vItemOpt = voucherItemRepository.findById(voucherId);
            if (vItemOpt.isPresent()) {
                VoucherItem vItem = vItemOpt.get();
                if (vItem.getAccount().getId().equals(user.getId()) && vItem.getStatus() == 0) {
                    VoucherPercent vPercent = vItem.getVoucherPercent();
                    if (productTotal >= vPercent.getMinPrice()) {
                        if (vPercent.getVoucherType() != null && vPercent.getVoucherType() == true) {
                            // Discount Voucher: applied to product total
                            voucherDiscount = productTotal * (vPercent.getPercentVoucher() / 100.0);
                        } else {
                            // Free Shipping Voucher: applied to shipping fee
                            voucherDiscount = shippingFee * (vPercent.getPercentVoucher() / 100.0);
                        }

                        if (vPercent.getMaxPrice() != null && voucherDiscount > vPercent.getMaxPrice()) {
                            voucherDiscount = vPercent.getMaxPrice();
                        }
                        vItem.setStatus(1); // Mark as used
                        voucherItemRepository.save(vItem);
                    }
                }
            }
        }
        order.setVoucherDiscount(voucherDiscount);
        finalTotalAmount -= (long) voucherDiscount;

        order.setTotalAmount((double) finalTotalAmount);
        orderRepository.save(order);

        // 4. Handle Payment
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());

        if ("QR".equals(paymentMethod)) {
            try {
                String baseUrl = getBaseUrl(request);
                String returnUrl = baseUrl + "/payment/success";
                String cancelUrl = baseUrl + "/payment/cancel";

                // PayOS orderCode must be a number. Use timestamp to ensure uniqueness
                long orderCode = System.currentTimeMillis();
                order.setOrderCode(orderCode);
                orderRepository.save(order);

                CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount(finalTotalAmount)
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
