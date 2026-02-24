package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryManagerController {
    @GetMapping("/admin/category-manager")
    public String categoryManager() {
        return "views/admin/manage-categories";
    }
}
