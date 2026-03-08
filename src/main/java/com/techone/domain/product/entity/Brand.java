package com.techone.domain.product.entity;

import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "brands")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @NotBlank(message = "Tên không được để trống")
    @Column(columnDefinition = "nvarchar(255)")
    public String name;

    @NotBlank(message = "Logo không được để trống")
    @Column(columnDefinition = "varchar(255)")
    public String logo;

    public Boolean status = true;

    @OneToMany(mappedBy = "brand")
    @lombok.ToString.Exclude
    public List<Product> product;
}
