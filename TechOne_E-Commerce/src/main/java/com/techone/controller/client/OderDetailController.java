package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OderDetailController {
	@GetMapping("/client/order_detail")
	public String orderDetail() {
		return "client/order_detail";
	}
}
