package com.techone.domain.order.service;

import com.techone.domain.order.entity.Order;
import com.techone.domain.order.entity.OrderDetail;
import com.techone.domain.order.entity.Cart;
import com.techone.domain.order.entity.CartItem;
import com.techone.domain.order.entity.Shipment;
import com.techone.domain.order.repository.*;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Address;
import com.techone.domain.user.repository.AddressRepository;
import com.techone.domain.product.entity.Variant;
import com.techone.domain.product.repository.VariantRepository;
import com.techone.domain.promotion.entity.VoucherItem;
import com.techone.domain.promotion.entity.VoucherPercent;
import com.techone.domain.promotion.repository.VoucherItemRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final VoucherItemRepository voucherItemRepository;
    private final VariantRepository variantRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingFeeService shippingFeeService;
    private final OrderEmailService orderEmailService;

    @Override
    @Transactional
    public Order createOrder(Account user, List<Integer> itemIds, Integer addressId, String paymentMethod,
            Integer voucherId, String note,
            HttpSession session) throws Exception {
        if (addressId == null)
            throw new IllegalArgumentException("Vui lòng chọn địa chỉ giao hàng");
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Địa chỉ không tồn tại hoặc đã bị xóa"));
        Cart cart = cartRepository.findByAccountId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng của bạn"));
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (itemIds != null && !itemIds.isEmpty()) {
            cartItems = cartItems.stream().filter(item -> itemIds.contains(item.getId())).toList();
        }
        if (cartItems.isEmpty())
            throw new IllegalArgumentException("Giỏ hàng của bạn đang trống hoặc không có sản phẩm nào được chọn");

        long productTotalForVoucher = 0;
        for (CartItem item : cartItems) {
            Variant variant = item.getVariant();
            if (variant.getStock() == null || variant.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm '" + variant.getProduct().getName() + " ["
                        + variant.getVariantName() + "]' đã hết hàng hoặc không đủ số lượng");
            }
            productTotalForVoucher += (long) (variant.getPrice() * (1 - variant.getDiscount() / 100.0)
                    * item.getQuantity());
        }

        VoucherItem appliedVoucher = null;
        if (voucherId != null) {
            appliedVoucher = voucherItemRepository.findById(voucherId)
                    .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));
            if (!appliedVoucher.getAccount().getId().equals(user.getId()))
                throw new IllegalArgumentException("Voucher này không thuộc về tài khoản của bạn");
            if (appliedVoucher.getStatus() != 0)
                throw new IllegalArgumentException("Voucher này đã được sử dụng");
            VoucherPercent vPercent = appliedVoucher.getVoucherPercent();
            if (productTotalForVoucher < vPercent.getMinPrice())
                throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để sử dụng Voucher này");
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(vPercent.getActiveDay()) || now.isAfter(vPercent.getEndAt()))
                throw new IllegalArgumentException("Voucher này đã hết hạn hoặc chưa đến thời gian áp dụng");
        }

        Order order = new Order();
        order.setCreateAt(LocalDateTime.now());
        if ("QR".equalsIgnoreCase(paymentMethod))
            order.setStatus(1);
        else
            order.setStatus(0);
        order.setNote(note);
        order.setAccount(user);
        order.setAppliedVoucher(appliedVoucher);
        order = orderRepository.save(order);

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setAddress(address);
        shipment.setStatus(0);
        shipment.setCreateAt(LocalDateTime.now());
        shipmentRepository.save(shipment);

        Integer shippingFee = shippingFeeService.calculateShippingFee(null, address.getWard().getDistrict().getId(),
                address.getWard().getCode());
        order.setShippingFee(shippingFee.doubleValue());

        long productTotal = 0;
        List<OrderDetail> details = new java.util.ArrayList<>();
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setVariant(item.getVariant());
            detail.setQuantity(item.getQuantity());
            double pricePerItem = item.getVariant().getPrice() * (1 - item.getVariant().getDiscount() / 100.0);
            detail.setUnitPrice(pricePerItem);
            orderDetailRepository.save(detail);
            details.add(detail);
            Variant variant = item.getVariant();
            variant.setStock(variant.getStock() - item.getQuantity());
            variantRepository.save(variant);
            productTotal += (long) (pricePerItem * item.getQuantity());
        }
        order.setOrderDetail(details);

        double voucherDiscount = 0;
        if (appliedVoucher != null) {
            VoucherPercent vPercent = appliedVoucher.getVoucherPercent();
            if (vPercent.getVoucherType() != null && vPercent.getVoucherType())
                voucherDiscount = productTotal * (vPercent.getPercentVoucher() / 100.0);
            else
                voucherDiscount = shippingFee * (vPercent.getPercentVoucher() / 100.0);
            if (vPercent.getMaxPrice() != null && voucherDiscount > vPercent.getMaxPrice())
                voucherDiscount = vPercent.getMaxPrice();
            if (order.getStatus() == 0) {
                appliedVoucher.setStatus(1);
                voucherItemRepository.save(appliedVoucher);
            }
        }
        order.setVoucherDiscount(voucherDiscount);
        order.setTotalAmount((double) (productTotal + shippingFee - voucherDiscount));
        order = orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);
        session.setAttribute("cartCount", 0);
        if (order.getStatus() == 0) {
            try {
                orderEmailService.sendOrderInvoice(order);
            } catch (Exception e) {
            }
        }
        return order;
    }

    @Override
    @Transactional
    public List<Integer> cancelOrder(Integer orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getStatus() != null && order.getStatus() == 1)
                return performCancel(order);
        }
        return java.util.Collections.emptyList();
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(3);
        List<Order> expiredOrders = orderRepository.findAllByOrderByCreateAtDesc().stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                .filter(o -> o.getCreateAt().isBefore(expirationTime)).toList();
        for (Order order : expiredOrders)
            performCancel(order);
    }

    private List<Integer> performCancel(Order order) {
        if (order.getOrderDetail() != null) {
            for (OrderDetail detail : order.getOrderDetail()) {
                Variant variant = detail.getVariant();
                if (variant != null) {
                    variant.setStock(variant.getStock() + detail.getQuantity());
                    variantRepository.save(variant);
                }
            }
        }
        if (order.getAppliedVoucher() != null) {
            VoucherItem voucher = order.getAppliedVoucher();
            voucher.setStatus(0);
            voucherItemRepository.save(voucher);
        }
        List<Integer> restoredItemIds = new java.util.ArrayList<>();
        Account account = order.getAccount();
        if (account != null) {
            Cart cart = cartRepository.findByAccountId(account.getId()).orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setAccount(account);
                return cartRepository.save(newCart);
            });
            if (order.getOrderDetail() != null) {
                for (OrderDetail detail : order.getOrderDetail()) {
                    Variant variant = detail.getVariant();
                    Optional<CartItem> existingItem = cartItemRepository.findByCartAndVariant(cart, variant);
                    if (existingItem.isPresent()) {
                        CartItem item = existingItem.get();
                        item.setQuantity(item.getQuantity() + detail.getQuantity());
                        cartItemRepository.save(item);
                        restoredItemIds.add(item.getId());
                    } else {
                        CartItem newItem = new CartItem();
                        newItem.setCart(cart);
                        newItem.setVariant(variant);
                        newItem.setQuantity(detail.getQuantity());
                        newItem.setStatus(1);
                        newItem.setCreateAt(LocalDateTime.now());
                        cartItemRepository.save(newItem);
                        restoredItemIds.add(newItem.getId());
                    }
                }
            }
        }
        orderRepository.delete(order);
        return restoredItemIds;
    }
}

