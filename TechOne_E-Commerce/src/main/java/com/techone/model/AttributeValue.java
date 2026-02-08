package com.techone.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
	
	@Column(columnDefinition = "varchar(255)")
	@NotBlank(message = "Giá trị không thể để trống")
	public String value;
	
	@ManyToOne
	@JoinColumn(name = "attribute_id")
	Attribute attribute;
	
	@OneToMany(mappedBy = "attributeValue")
	public List<VariantAttributeValue> variantAttributeValue;
}
