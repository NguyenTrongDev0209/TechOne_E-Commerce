package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductFormController {
	
	@GetMapping("/admin/product-list/product-form")
	public String shippingConfiguration() {
		return "admin/product-form";
	}

}
