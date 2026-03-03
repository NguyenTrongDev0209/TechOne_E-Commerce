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

    @org.springframework.data.jpa.repository.Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart " +
            "AND ci.variant.status = true AND ci.variant.product.status = true " +
            "ORDER BY ci.createAt DESC")
    List<CartItem> findActiveItemsByCart(@org.springframework.data.repository.query.Param("cart") Cart cart);

    Optional<CartItem> findByCartAndVariant(Cart cart, Variant variant);

    @org.springframework.data.jpa.repository.Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.variant.id = :variantId")
    Optional<CartItem> findByCartIdAndVariantId(
            @org.springframework.data.repository.query.Param("cartId") Integer cartId,
            @org.springframework.data.repository.query.Param("variantId") Integer variantId);

    void deleteByVariant(Variant variant);

    boolean existsByVariant(Variant variant);
}
