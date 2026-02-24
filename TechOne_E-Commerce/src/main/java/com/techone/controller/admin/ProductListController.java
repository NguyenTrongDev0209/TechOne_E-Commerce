package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ProductListController {
		
	@GetMapping("/admin/product-list")
	public String shippingConfiguration() {
		return "views/admin/product-list";
	}
	
}
