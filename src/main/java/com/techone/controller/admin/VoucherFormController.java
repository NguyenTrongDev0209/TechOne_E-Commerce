package com.techone.controller.admin;

import com.techone.model.VoucherItem;
import com.techone.model.VoucherPercent;
import com.techone.repository.AccountRepository;
import com.techone.repository.VoucherItemRepository;
import com.techone.repository.VoucherPercentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VoucherFormController {

    @Autowired
    private VoucherPercentRepository voucherPercentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VoucherItemRepository voucherItemRepository;

    @GetMapping("/admin/voucher-list/voucher-form")
    public String voucherForm(@RequestParam(value = "id", required = false) Integer id, Model model) {
        VoucherPercent voucher;
        if (id != null) {
            voucher = voucherPercentRepository.findById(id).orElse(new VoucherPercent());
        } else {
            voucher = new VoucherPercent();
        }
        model.addAttribute("voucherPercent", voucher);
        return "views/admin/voucher-form";
    }

    @PostMapping("/admin/voucher-list/save")
    public String saveVoucher(@Valid @ModelAttribute("voucherPercent") VoucherPercent voucherPercent,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "views/admin/voucher-form";
        }

        boolean isNew = voucherPercent.getId() == null;

        if (!isNew) {
            voucherPercentRepository.findById(voucherPercent.getId()).ifPresent(existing -> {
                voucherPercent.setCreateAt(existing.getCreateAt());
            });
        }

        VoucherPercent savedVoucher = voucherPercentRepository.save(voucherPercent);

        if (isNew) {
            accountRepository.findAll().forEach(account -> {
                VoucherItem item = new VoucherItem();
                item.setVoucherPercent(savedVoucher);
                item.setAccount(account);
                item.setStatus(0); // 0: Chưa sử dụng
                voucherItemRepository.save(item);
            });
        }

        return "redirect:/admin/voucher-list";
    }

}
