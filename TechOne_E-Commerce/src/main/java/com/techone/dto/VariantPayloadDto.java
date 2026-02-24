package com.techone.dto;

import java.util.Map;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class VariantPayloadDto {
    private Long id; // Temp ID from frontend tracking

    @NotBlank(message = "Mã SKU không được trống")
    private String sku;

    @NotNull(message = "Số lượng không được trống")
    @Min(value = 0, message = "Số lượng không thể âm")
    private Integer stock;

    @NotNull(message = "Giá gốc không được trống")
    @Min(value = 0, message = "Giá không thể âm")
    private Double price;

    @NotNull(message = "Giảm giá không được trống")
    @Min(value = 0, message = "Giảm giá không thể âm")
    @Max(value = 100, message = "Giảm giá phải nhỏ hơn hoặc bằng 100%")
    private Double discount;

    private String imageInputName;
    private Map<String, String> attributes; // Key: Attribute Name, Value: Attribute Value
}
