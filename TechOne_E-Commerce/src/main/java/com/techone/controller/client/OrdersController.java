package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrdersController {
	
	@GetMapping("/account/orders")
	public String showOders() {
		return "fragments/client/orders";
	}
}
