package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionManagerController {

    @GetMapping("/admin/transaction-manager")
    public String transactionManager() {
        return "views/admin/transaction-manager";
    }
}
