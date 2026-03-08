package com.techone.domain.post.entity;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.user.entity.Account;
import com.techone.domain.product.entity.Category;
import com.techone.domain.user.entity.Favourite;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Tiêu đề không được để trống")
    public String title;

    @Column(columnDefinition = "nvarchar(max)")
    @NotBlank(message = "Nội dung không được để trống")
    public String content;

    @Column(columnDefinition = "nvarchar(255)")
    public String thumbnail;

    public Boolean status = true;

    @PastOrPresent(message = "Ngày tạo không thể ở tương lai")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    public LocalDate createAt = LocalDate.now();

    public Integer viewCount = 0;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @NotNull(message = "Vui lòng chọn danh mục bài viết")
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Comment> comment;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ImagePost> imagePost;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Favourite> favourite;
}
