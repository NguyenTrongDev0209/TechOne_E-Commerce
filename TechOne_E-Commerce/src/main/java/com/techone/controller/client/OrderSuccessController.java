package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderSuccessController {
	
	@GetMapping("/checkout/order-success")
	public String showSuccessOrder() {
		return "views/client//order-success";
	}
}
