package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyOrderController {
	@GetMapping("/client/my_orders")
	public String myOder() {
		return "client/my_orders";
	}
}
