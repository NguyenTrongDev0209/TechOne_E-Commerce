package com.techone.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VoucherController {

    @GetMapping("/account/vouchers")
    public String showVouchers() {
        return "views/client/voucher";
    }
}
