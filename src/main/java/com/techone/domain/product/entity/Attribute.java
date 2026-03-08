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
@Table(name = "attributes")
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Tên không được để trống")
    public String name;

    @OneToMany(mappedBy = "attribute")
    public List<AttributeValue> attributeValue;
}
