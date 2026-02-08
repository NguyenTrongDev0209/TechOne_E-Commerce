package com.techone.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "events")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Tên event không được để trống")
	public String name;
	
	@PastOrPresent(message = "Ngày tạo phải là quá khứ hoặc hiện tại")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime createAt = LocalDateTime.now();
	
	@Future(message = "Ngày kết thúc phải ở tương lai")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime endAt;
	
	public Integer status;
	
	@OneToMany(mappedBy = "event")
	public List<EventImage> eventImage;
	
	@OneToMany(mappedBy = "event")
	public List<Sale> sale;
}
