package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VoucherFormController {

    @GetMapping("/admin/voucher-list/voucher-form")
    public String voucherForm() {
        return "views/admin/voucher-form";
    }

}
