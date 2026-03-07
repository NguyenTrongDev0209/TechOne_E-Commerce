package com.techone.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

@Entity
@Table(name = "provinces")
public class Province {
	@Id
	@Column(name = "id")
	public Integer id;

	@Column(columnDefinition = "varchar(255)")
	public String code;

	@Column(columnDefinition = "nvarchar(255)")
	public String name;

	@JsonIgnore
	@OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
	public List<District> district;
}
