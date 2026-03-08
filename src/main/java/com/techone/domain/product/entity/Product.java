package com.techone.domain.product.entity;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.user.entity.Account;
import com.techone.domain.product.entity.Review;
import com.techone.domain.product.entity.Sale;
import com.techone.domain.product.entity.Specification;
import com.techone.domain.product.entity.ImageProduct;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Tên sản phẩm không được để trống")
    public String name;

    @Column(columnDefinition = "varchar(255)")
    @NotBlank(message = "Mã SKU không được trống")
    public String slug;

    @PastOrPresent(message = "Ngày tạo phải là ngày trong quá khứ hoặc hiện tại")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    public LocalDateTime createAt = LocalDateTime.now();

    public Boolean status;
    public Integer stockStatus;

    @Column(name = "content", columnDefinition = "nvarchar(max)")
    @NotBlank(message = "Mô tả sản phẩm không được trống")
    public String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @jakarta.validation.constraints.NotNull(message = "Chưa chọn danh mục")
    @lombok.ToString.Exclude
    Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    @jakarta.validation.constraints.NotNull(message = "Chưa chọn thương hiệu")
    @lombok.ToString.Exclude
    Brand brand;

    public String specifications;

    @ManyToOne
    @JoinColumn(name = "account_id")
    public Account account;

    @OneToMany(mappedBy = "product")
    @lombok.ToString.Exclude
    public List<Sale> sale;

    @OneToMany(mappedBy = "product")
    @lombok.ToString.Exclude
    public List<Review> review;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    public List<Specification> specificationList;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @lombok.ToString.Exclude
    public List<Variant> variant;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    public List<ImageProduct> imageProduct;
}
