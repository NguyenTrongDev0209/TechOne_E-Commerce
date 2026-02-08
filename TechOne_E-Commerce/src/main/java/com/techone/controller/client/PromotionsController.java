package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PromotionsController {
	
	@GetMapping("/promotions")
	public String showPromotions() {
		return "views/client/promotions";
	}
	
}
