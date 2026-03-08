package com.techone.domain.product.entity;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.post.entity.Post; // TEMPORARY

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Tên danh mục không được để trống")
    public String name;

    @Column(columnDefinition = "varchar(255)")
    public String slug;

    public Boolean status = true;

    @PastOrPresent(message = "Ngày tạo không thể ở tương lai")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    public LocalDate createAt = LocalDate.now();

    public Boolean type = true;

    @Column(columnDefinition = "nvarchar(255)")
    public String image;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @lombok.ToString.Exclude
    public Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    @lombok.ToString.Exclude
    public List<Category> children;

    @OneToMany(mappedBy = "category")
    @lombok.ToString.Exclude
    public List<Post> post;

    @OneToMany(mappedBy = "category")
    @lombok.ToString.Exclude
    public List<Product> product;
}

