package com.techone.domain.promotion.entity;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "voucher_percent")
public class VoucherPercent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Tên voucher không được để trống")
    public String name;

    @Column(columnDefinition = "varchar(100)")
    @NotBlank(message = "Code voucher không được để trống")
    public String code;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    public LocalDateTime createAt = LocalDateTime.now();

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu không thể ở quá khứ")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    public LocalDateTime activeDay;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải ở tương lai")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    public LocalDateTime endAt;

    public Boolean voucherType;

    @NotNull(message = "Giá trị đơn hàng tối thiểu không được để trống")
    @PositiveOrZero(message = "Giá tối thiểu không thể âm")
    public Double minPrice;

    public Double maxPrice;

    public Integer status;

    @NotNull(message = "Giá trị giảm không được để trống")
    @PositiveOrZero(message = "Giá trị giảm không thể âm")
    @Max(value = 100, message = "Giá trị giảm không thể vượt quá 100%")
    public Double percentVoucher;

    public Integer quantity;

    @OneToMany(mappedBy = "voucherPercent")
    public List<VoucherItem> voucherList;
}
