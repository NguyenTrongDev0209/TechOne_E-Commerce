package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotFoundController {
	@GetMapping("/client/notfount")
	public String notFound() {
		return "client/notfount";
	}
}
