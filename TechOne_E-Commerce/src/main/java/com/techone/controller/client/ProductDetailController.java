package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductDetailController {
	
	@GetMapping("/product/product-detail")
	public String showProductDetail() {
		return "fragments/client/product-detail";
	}
}
