package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EventManagerController {

    @GetMapping("/admin/event-manager")
    public String eventManager() {
        return "views/admin/event-manager";
    }

    @GetMapping("/admin/event-manager/event-form")
    public String eventForm() {
        return "views/admin/event-form";
    }
}
