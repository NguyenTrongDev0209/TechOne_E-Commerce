package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OderDetailController {
	
	@GetMapping("/account/orders/order-detail")
	public String orderDetail() {
		return "fragments/client/order-detail";
	}
}