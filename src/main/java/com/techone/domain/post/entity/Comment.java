package com.techone.domain.post.entity;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.user.entity.Account;

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
