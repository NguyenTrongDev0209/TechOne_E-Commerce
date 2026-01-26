package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfirmOtpController {
	
	@GetMapping("/forgot-password/confirm-otp")
	public String showConfirmOtp() {
		return "fragments/client/confirm-otp";
	}
	
}
