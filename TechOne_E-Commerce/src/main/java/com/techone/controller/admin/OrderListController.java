package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderListController {
	
	@GetMapping("/admin/order-list")
	public String shippingConfiguration() {
		return "views/admin/order-list";
	}
	
}
