package com.techone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationItemDto {
    @NotBlank(message = "Tên thông số không được bỏ trống")
    private String name;

    @NotBlank(message = "Giá trị thông số không được bỏ trống")
    private String value;
}
