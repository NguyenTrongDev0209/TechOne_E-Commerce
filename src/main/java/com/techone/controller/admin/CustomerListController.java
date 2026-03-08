package com.techone.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.techone.domain.user.entity.Account;
import com.techone.domain.user.repository.AccountRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomerListController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/admin/customer-list")
    public String shippingConfiguration(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "dateFrom", required = false) String dateFromStr,
            @RequestParam(value = "dateTo", required = false) String dateToStr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Integer status = null;
        if ("active".equals(statusStr))
            status = 1;
        else if ("locked".equals(statusStr))
            status = 0;

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.LocalDate dateFrom = null;
        if (dateFromStr != null && !dateFromStr.trim().isEmpty()) {
            try {
                dateFrom = java.time.LocalDate.parse(dateFromStr.trim(), formatter);
            } catch (Exception e) {
            }
        }

        java.time.LocalDate dateTo = null;
        if (dateToStr != null && !dateToStr.trim().isEmpty()) {
            try {
                dateTo = java.time.LocalDate.parse(dateToStr.trim(), formatter);
            } catch (Exception e) {
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Account> customersPage = accountRepository.findCustomersByFilters(
                (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                status,
                dateFrom,
                dateTo,
                pageable);

        model.addAttribute("customers", customersPage);
        model.addAttribute("paramSearch", search);
        model.addAttribute("paramStatus", statusStr == null ? "all" : statusStr);
        model.addAttribute("paramDateFrom", dateFromStr);
        model.addAttribute("paramDateTo", dateToStr);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // Stats
        model.addAttribute("totalCustomers", accountRepository.countByRole(false));
        model.addAttribute("activeCustomers", accountRepository.countByRoleAndStatus(false, 1));
        model.addAttribute("lockedCustomers", accountRepository.countByRoleAndStatus(false, 0));

        return "views/admin/customer-list";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/customer/update")
    public String updateCustomer(@org.springframework.web.bind.annotation.ModelAttribute Account accountForm,
            @org.springframework.web.bind.annotation.RequestParam(value = "id") Integer id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        java.util.Optional<Account> optAccount = accountRepository.findById(id);
        if (optAccount.isPresent()) {
            Account account = optAccount.get();

            // Update basic info
            account.setFullname(accountForm.getFullname());
            account.setPhone(accountForm.getPhone());
            account.setEmail(accountForm.getEmail());
            account.setBirthday(accountForm.getBirthday());
            account.setGender(accountForm.getGender());
            account.setStatus(accountForm.getStatus());

            accountRepository.save(account);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin khách hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
        }

        return "redirect:/admin/customer-list";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/customer/toggle-status")
    public String toggleCustomerStatus(@org.springframework.web.bind.annotation.RequestParam("id") Integer id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        java.util.Optional<Account> optAccount = accountRepository.findById(id);
        if (optAccount.isPresent()) {
            Account account = optAccount.get();

            // Toggle status (1 becomes 0, 0 becomes 1)
            int newStatus = (account.getStatus() != null && account.getStatus() == 1) ? 0 : 1;
            account.setStatus(newStatus);

            accountRepository.save(account);

            String statusMsg = newStatus == 1 ? "đã được mở khóa" : "đã bị khóa";
            redirectAttributes.addFlashAttribute("success",
                    "Tài khoản " + account.getFullname() + " " + statusMsg + "!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
        }

        return "redirect:/admin/customer-list";
    }
}
