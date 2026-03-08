package com.techone.domain.post.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "images_posts")
public class ImagePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "varchar(255)")
    public String pathImage;

    public LocalDate createAt = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "post_id")
    Post post;
}
