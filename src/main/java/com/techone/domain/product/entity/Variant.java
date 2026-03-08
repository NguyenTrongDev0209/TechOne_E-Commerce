package com.techone.domain.product.entity;

import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Favourite;
import com.techone.domain.order.entity.CartItem;
import com.techone.domain.order.entity.OrderDetail;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "variants")
public class Variant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @PositiveOrZero(message = "Giá không thể âm")
    public Double price;

    @PositiveOrZero(message = "Số lượng tồn không thể âm")
    public Integer stock;

    @PositiveOrZero(message = "Giảm giá không thể âm")
    public Double discount;

    public Boolean status;

    @Column(columnDefinition = "varchar(255)")
    @NotBlank(message = "Mã SKU không thể bỏ trống")
    public String sku;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @OneToMany(mappedBy = "variant")
    public List<Favourite> favourite;

    @OneToMany(mappedBy = "variant")
    public List<CartItem> cartItem;

    @OneToMany(mappedBy = "variant")
    public List<OrderDetail> orderDetail;

    @OneToMany(mappedBy = "variant", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    public List<VariantImage> variantImages;

    @OneToMany(mappedBy = "variant", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    public List<VariantAttributeValue> variantAttributeValues;

    public String getVariantName() {
        if (variantAttributeValues == null || variantAttributeValues.isEmpty()) {
            return "";
        }
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < variantAttributeValues.size(); i++) {
            VariantAttributeValue vav = variantAttributeValues.get(i);
            if (vav.getAttributeValue() != null && vav.getAttributeValue().getAttribute() != null) {
                name.append(vav.getAttributeValue().getAttribute().getName())
                        .append(": ")
                        .append(vav.getAttributeValue().getValue());
                if (i < variantAttributeValues.size() - 1) {
                    name.append(" | ");
                }
            }
        }
        return name.toString();
    }
}
