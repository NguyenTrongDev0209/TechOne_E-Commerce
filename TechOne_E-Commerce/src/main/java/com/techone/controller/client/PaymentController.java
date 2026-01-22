package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {
	@GetMapping("/client/payments")
	public String payments() {
		return "client/payments";
	}
}
