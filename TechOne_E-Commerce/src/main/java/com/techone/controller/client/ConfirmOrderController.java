package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfirmOrderController {
	@GetMapping("/client/confirm_order")
	public String confirmOrder() {
		return "client/confirm_order";
	}
}
