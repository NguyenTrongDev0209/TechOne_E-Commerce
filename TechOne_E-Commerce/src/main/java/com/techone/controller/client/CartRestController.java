package com.techone.controller.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techone.model.Account;
import com.techone.model.Cart;
import com.techone.model.CartItem;
import com.techone.model.Variant;
import com.techone.repository.CartItemRepository;
import com.techone.repository.CartRepository;
import com.techone.repository.VariantRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private VariantRepository variantRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thêm vào giỏ hàng");
        }

        Object variantIdObj = payload.get("variantId");
        Object quantityObj = payload.get("quantity");

        Integer variantId = variantIdObj != null ? ((Number) variantIdObj).intValue() : null;
        Integer quantity = quantityObj != null ? ((Number) quantityObj).intValue() : null;

        if (variantId == null || quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }

        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (!variantOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Variant variant = variantOpt.get();
        if (variant.getStatus() == null || !variant.getStatus() ||
                variant.getProduct().getStatus() == null || !variant.getProduct().getStatus()) {
            return ResponseEntity.badRequest().body("Sản phẩm hiện không khả dụng");
        }

        // Get or Create Cart
        Cart cart = cartRepository.findByAccount(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setAccount(user);
            return cartRepository.save(newCart);
        });

        int quantityToAdd = quantity;
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId());
        CartItem cartItem;

        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            int currentInCart = cartItem.getQuantity();
            if (variant.getStock() < (currentInCart + quantityToAdd)) {
                return ResponseEntity.badRequest().body(
                        "Số lượng tồn kho không đủ (Bạn hiện đã có " + currentInCart + " sản phẩm này trong giỏ hàng)");
            }
            cartItem.setQuantity(currentInCart + quantityToAdd);
        } else {
            if (variant.getStock() < quantityToAdd) {
                return ResponseEntity.badRequest().body(
                        "Số lượng tồn kho không đủ (Sản phẩm này hiện chỉ còn " + variant.getStock() + " sản phẩm)");
            }
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setVariant(variant);
            cartItem.setQuantity(quantityToAdd);
            cartItem.setStatus(1); // Active
        }

        cartItemRepository.save(cartItem);

        int cartCount = cartItemRepository.findActiveItemsByCart(cart).size();
        session.setAttribute("cartCount", cartCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã thêm sản phẩm vào giỏ hàng");
        response.put("cartCount", cartCount);
        response.put("cartItemId", cartItem.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteItem(@RequestBody Map<String, Object> payload, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập");
        }

        Integer cartItemId = (Integer) payload.get("cartItemId");
        if (cartItemId == null) {
            return ResponseEntity.badRequest().body("ID không hợp lệ");
        }

        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent() && itemOpt.get().getCart().getAccount().getId().equals(user.getId())) {
            Cart cart = itemOpt.get().getCart();
            cartItemRepository.delete(itemOpt.get());

            int cartCount = cartItemRepository.findActiveItemsByCart(cart).size();
            session.setAttribute("cartCount", cartCount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartCount", cartCount);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update-quantity")
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> payload, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập");
        }

        Integer cartItemId = (Integer) payload.get("cartItemId");
        Integer quantity = (Integer) payload.get("quantity");

        if (cartItemId == null || quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }

        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent() && itemOpt.get().getCart().getAccount().getId().equals(user.getId())) {
            CartItem item = itemOpt.get();

            // Check stock
            if (item.getVariant().getStock() < quantity) {
                return ResponseEntity.badRequest().body("Số lượng tồn kho không đủ");
            }

            item.setQuantity(quantity);
            cartItemRepository.save(item);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newQuantity", quantity);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }
}
