package com.techone.controller.admin;

import com.techone.domain.promotion.repository.VoucherPercentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VoucherManagerController {
    @Autowired
    private VoucherPercentRepository voucherPercentRepository;

    @GetMapping("/admin/voucher-list")
    public String voucherList(Model model) {
        model.addAttribute("vouchers", voucherPercentRepository.findAll(Sort.by(Sort.Direction.DESC, "createAt")));
        return "views/admin/voucher-list";
    }
}

