package com.techone.domain.user.entity;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.techone.domain.post.entity.Post;
import com.techone.domain.post.entity.Comment;
import com.techone.domain.product.entity.Review;
import com.techone.domain.promotion.entity.VoucherItem;
import com.techone.domain.product.entity.Variant;
import com.techone.domain.order.entity.Cart;
import com.techone.domain.order.entity.Order;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "varchar(255)")
    public String provider;

    @Column(columnDefinition = "varchar(12)")
    public String phone;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Họ và tên không được để trống")
    public String fullname;

    @Transient
    public String contact;

    @Column(columnDefinition = "varchar(255)")
    public String email;

    @Column(columnDefinition = "varchar(255)")
    @NotBlank(message = "Password không được để trống")
    public String password;

    @Column(columnDefinition = "varchar(255)")
    public String avatar;

    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public LocalDate birthday;

    public Boolean gender;
    public Integer status;
    public Boolean role = false;

    @PastOrPresent(message = "Ngày tạo không thể ở tương lai")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    public LocalDate createAt;

    @OneToMany(mappedBy = "account")
    public List<Post> post;

    @OneToMany(mappedBy = "account")
    public List<Comment> comment;

    @OneToMany(mappedBy = "account")
    public List<Favourite> favourite;

    @OneToMany(mappedBy = "account")
    public List<Review> review;

    @OneToMany(mappedBy = "account")
    public List<ForgotPassword> forgotPassword;

    @OneToMany(mappedBy = "account")
    public List<VoucherItem> voucherList;

    @OneToMany(mappedBy = "account")
    public List<Variant> variant;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    public Cart cart;

    @OneToMany(mappedBy = "account")
    public List<Address> address;

    @OneToMany(mappedBy = "account")
    public List<Order> order;
}
