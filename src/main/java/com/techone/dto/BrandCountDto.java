package com.techone.dto;

import com.techone.domain.product.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandCountDto {
    private Brand brand;
    private long productCount;
}

