package com.techone.controller.authentic;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfirmOtpController {
	
	@GetMapping("/confirm-otp")
	public String showConfirmOtp() {
		return "views/authentic/confirm-otp";
	}

}
