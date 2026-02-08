package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostManagerController {
    @GetMapping("/admin/post-list")
    public String postList() {
        return "views/admin/post-list";
    }
}
