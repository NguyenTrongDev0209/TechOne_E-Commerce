package com.techone.domain.product.entity;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "variant_images")
public class VariantImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "varchar(255)")
    @NotBlank(message = "Hình ảnh không được trống")
    public String pathImage;

    @PastOrPresent(message = "Ngày tạo phải là ngày trong quá khứ hoặc hiện tại")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    public LocalDateTime createAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "variant_id")
    Variant variant;
}
