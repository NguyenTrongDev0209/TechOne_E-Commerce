package com.techone.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
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
	@Column(name = "id")
	public String id;

	@Column(columnDefinition = "varchar(255)")
	public String code;

	@Column(columnDefinition = "nvarchar(255)")
	public String name;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "district_id")
	District district;

	@JsonIgnore
	@OneToMany(mappedBy = "ward", cascade = CascadeType.ALL)
	public List<Address> address;
}
