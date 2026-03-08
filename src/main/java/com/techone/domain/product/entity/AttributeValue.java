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
@Table(name = "attribute_values")
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Giá trị không thể để trống")
    public String value;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    Attribute attribute;

    @OneToMany(mappedBy = "attributeValue")
    public List<VariantAttributeValue> variantAttributeValue;
}
