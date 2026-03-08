package com.techone.domain.product.entity;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.user.entity.Account;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public Integer rating;

    @Column(columnDefinition = "nvarchar(max)")
    public String comment;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    public LocalDateTime createAt = LocalDateTime.now();

    public Boolean status;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;
}
