package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FavouriteListController {
	@GetMapping("/client/fav_list")
	public String favouriteList() {
		return "client/fav_list";
	}
}
