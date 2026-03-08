package com.techone.domain.order.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.order.entity.Cart;
import com.techone.domain.order.entity.CartItem;
import com.techone.domain.product.entity.Variant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart " +
            "AND ci.variant.status = true AND ci.variant.product.status = true " +
            "ORDER BY ci.createAt DESC")
    List<CartItem> findActiveItemsByCart(@Param("cart") Cart cart);

    Optional<CartItem> findByCartAndVariant(Cart cart, Variant variant);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.variant.id = :variantId")
    Optional<CartItem> findByCartIdAndVariantId(@Param("cartId") Integer cartId, @Param("variantId") Integer variantId);

    void deleteByVariant(Variant variant);

    boolean existsByVariant(Variant variant);
}
