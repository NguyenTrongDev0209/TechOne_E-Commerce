package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FavouritedController {
	
	@GetMapping("/account/favourited")
	public String showFavourited() {
		return "views/client/favourited";
	}
}
