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
@Table(name = "wards")
public class Ward {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "varchar(255)")
	@NotBlank(message = "Code province không được để trống")
	public String code;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Tên quận/huyện không được để trống")
	public String name;
	
	@ManyToOne
	@JoinColumn(columnDefinition = "district_id")
	District district;
	
	@OneToMany(mappedBy = "ward")
	public List<Address> address;
}
