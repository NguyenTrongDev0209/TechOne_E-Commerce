package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VoucherManagerController {
    @GetMapping("/admin/voucher-list")
    public String voucherList() {
        return "views/admin/voucher-list";
    }
}
