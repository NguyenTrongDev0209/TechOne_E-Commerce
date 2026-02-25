package com.techone.controller.client;

import com.techone.model.Account;
import com.techone.model.VoucherItem;
import com.techone.repository.VoucherItemRepository;
import com.techone.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class VoucherController {

    @Autowired
    private VoucherItemRepository voucherItemRepository;

    @GetMapping("/account/vouchers")
    public String showVouchers(Model model) {
        Account user = SessionUtils.get("user");
        List<VoucherItem> voucherItems;
        if (user != null) {
            voucherItems = voucherItemRepository.findByAccount(user);
        } else {
            voucherItems = Collections.emptyList();
        }
        model.addAttribute("voucherItems", voucherItems);
        return "views/client/voucher";
    }
}
