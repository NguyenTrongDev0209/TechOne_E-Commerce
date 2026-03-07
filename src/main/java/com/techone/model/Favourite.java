package com.techone.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "favourites")
public class Favourite {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
	Account account;
	
	@ManyToOne
	@JoinColumn(name = "post_id")
	Post post;
	
	@ManyToOne
	@JoinColumn(name = "variant_id")
	Variant variant;
}
