package com.techone.domain.order.entity;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.product.entity.Variant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(name = "UK_cart_variant", columnNames = { "cart_id", "variant_id" })
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @PositiveOrZero(message = "Số lượng không thể âm")
    public Integer quantity;

    public Integer status;

    @PastOrPresent(message = "Ngày tạo không thể ở tương lai")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    public LocalDateTime createAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "variant_id", unique = false)
    Variant variant;

    @ManyToOne
    @JoinColumn(name = "cart_id", unique = false)
    Cart cart;
}
