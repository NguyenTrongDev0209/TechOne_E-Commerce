package com.techone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techone.model.Cart;
import com.techone.model.CartItem;
import com.techone.model.Variant;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndVariant(Cart cart, Variant variant);

    Optional<CartItem> findByCartIdAndVariantId(Integer cartId, Integer variantId);

    void deleteByVariant(Variant variant);
}
