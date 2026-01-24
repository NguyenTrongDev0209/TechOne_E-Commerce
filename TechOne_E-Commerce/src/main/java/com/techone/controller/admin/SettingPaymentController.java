package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingPaymentController {

	@GetMapping("/admin/setting/setting-payment")
	public String shippingConfiguration() {
		return "admin/setting-payment";
	}
	
}
