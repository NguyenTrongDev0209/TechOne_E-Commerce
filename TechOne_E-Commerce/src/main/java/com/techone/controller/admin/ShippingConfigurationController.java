package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShippingConfigurationController {
	@GetMapping("/admin/shipping_configuration")
	public String shippingConfiguration() {
		return "admin/shipping_configuration";
	}
}
