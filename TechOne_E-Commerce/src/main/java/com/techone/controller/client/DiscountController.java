package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DiscountController {
	@GetMapping("/client/discount")
	public String discount() {
		return "client/discount";
	}
}
