package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductDetailController {
	@GetMapping("/client/product_detail")
	public String productDetail() {
		return "client/product_detail";
	}
}
