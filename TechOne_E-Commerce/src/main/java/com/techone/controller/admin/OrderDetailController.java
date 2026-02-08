package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderDetailController {
	
	@GetMapping("/admin/order-list/order-detail")
	public String shippingConfiguration() {
		return "views/admin/order-detail";
	}
	
}
