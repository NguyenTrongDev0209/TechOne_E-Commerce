package com.techone.domain.product.entity;

import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "specifications_Titles")
public class SpecificationTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "specifications_id")
    private Specification specification;

    @Column(columnDefinition = "nvarchar(255)")
    private String name;

    @OneToMany(mappedBy = "specificationTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<SpecificationValue> specificationValues;
}
