package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BrandManagerController {

    @GetMapping("/admin/brand-manager")
    public String brandManager() {
        return "views/admin/manage-brands";
    }
}
