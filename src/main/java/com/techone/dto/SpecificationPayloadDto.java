package com.techone.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationPayloadDto {
    @NotBlank(message = "Tên nhóm thông số không được bỏ trống")
    private String groupName;

    @NotNull(message = "Danh sách thông số không được null")
    @Valid
    private List<SpecificationItemDto> items;
}
