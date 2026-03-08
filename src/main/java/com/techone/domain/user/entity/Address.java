package com.techone.domain.user.entity;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "addresss")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(columnDefinition = "nvarchar(255)")
    @NotBlank(message = "Địa chỉ cụ thể không thể để trống")
    public String fullAddress;

    @PastOrPresent(message = "Ngày tạo địa chỉ không thể ở tương lai")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    public LocalDateTime createAt = LocalDateTime.now();

    public Boolean status;

    @Column(columnDefinition = "nvarchar(100)")
    @NotBlank(message = "Tên không được để trống")
    public String name;

    @Column(columnDefinition = "varchar(12)")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Số điện thoại không đúng định dạng")
    public String phone;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    Ward ward;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    public String toJson() {
        return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"phone\":\"%s\",\"fullAddress\":\"%s\",\"status\":%b," +
                        "\"ward\":{\"id\":\"%s\",\"name\":\"%s\"," +
                        "\"district\":{\"id\":%d,\"name\":\"%s\"," +
                        "\"province\":{\"id\":%d,\"name\":\"%s\"}}}}",
                id, name.replace("\"", "\\\""), phone, fullAddress.replace("\"", "\\\""), status,
                ward.getId(), ward.getName().replace("\"", "\\\""),
                ward.getDistrict().getId(), ward.getDistrict().getName().replace("\"", "\\\""),
                ward.getDistrict().getProvince().getId(),
                ward.getDistrict().getProvince().getName().replace("\"", "\\\""));
    }
}
