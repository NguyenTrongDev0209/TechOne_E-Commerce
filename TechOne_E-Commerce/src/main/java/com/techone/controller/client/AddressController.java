package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddressController {
	
	@GetMapping("/account/address")
	public String showAddress() {
		return "fragments/client/address";
	}
}
