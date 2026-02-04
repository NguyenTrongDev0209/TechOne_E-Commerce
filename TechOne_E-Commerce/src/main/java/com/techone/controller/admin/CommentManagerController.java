package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommentManagerController {

    @GetMapping("/admin/comment-list")
    public String commentList() {
        return "views/admin/comment-list";
    }
}
