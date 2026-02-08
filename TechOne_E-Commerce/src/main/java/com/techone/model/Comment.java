package com.techone.model;

import java.time.LocalDateTime;
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
@Table(name = "comments")
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(max)")
	@NotBlank(message = "Nội dung comment không được để trống")
	public String content;
	
	public LocalDateTime createAt = LocalDateTime.now();
	
	public Boolean status = true;
	//true = active; false = nonactive
	
	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Comment parent;
	
	@OneToMany(mappedBy = "parent")
	public List<Comment> children;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
	Account account;
	
	@ManyToOne
	@JoinColumn(name = "post_id")
	Post post;
}
