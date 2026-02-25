package com.techone.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "districts")
public class District {
	@Id
	@Column(name = "id")
	public Integer id;

	@Column(columnDefinition = "varchar(255)")
	public String code;

	@Column(columnDefinition = "nvarchar(255)")
	public String name;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "province_id")
	Province province;

	@JsonIgnore
	@OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
	public List<Ward> ward;
}
