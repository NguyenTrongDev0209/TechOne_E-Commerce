package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingShipmentController {
	
	@GetMapping("/admin/setting/setting-shipment")
	public String shippingConfiguration() {
		return "views/admin/setting-shipment";
	}
}
