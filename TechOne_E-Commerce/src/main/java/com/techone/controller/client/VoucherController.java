package com.techone.controller.client;

import com.techone.model.Account;
import com.techone.model.VoucherItem;
import com.techone.model.VoucherPercent;
import com.techone.repository.VoucherItemRepository;
import com.techone.repository.VoucherPercentRepository;
import com.techone.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class VoucherController {

    @Autowired
    private VoucherItemRepository voucherItemRepository;

    @Autowired
    private VoucherPercentRepository voucherPercentRepository;

    @GetMapping("/account/vouchers")
    public String showVouchers(Model model) {
        Account user = SessionUtils.get("user");
        List<VoucherItem> voucherItems;
        if (user != null) {
            voucherItems = voucherItemRepository.findByAccountAndStatus(user, 0);
        } else {
            voucherItems = Collections.emptyList();
        }
        model.addAttribute("voucherItems", voucherItems);
        model.addAttribute("now", LocalDateTime.now());
        return "views/client/voucher";
    }

    @PostMapping("/vouchers/apply-code")
    @ResponseBody
    public ResponseEntity<?> applyVoucherCode(@RequestBody Map<String, String> payload, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để sử dụng voucher"));
        }

        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập mã voucher"));
        }

        List<VoucherPercent> vouchersFound = voucherPercentRepository.findByCode(code.trim());
        if (vouchersFound.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Mã voucher không tồn tại"));
        }

        LocalDateTime now = LocalDateTime.now();
        VoucherPercent voucher = vouchersFound.stream()
                .filter(v -> v.getStatus() != null && v.getStatus() == 1)
                .filter(v -> !now.isBefore(v.getActiveDay()))
                .filter(v -> !now.isAfter(v.getEndAt()))
                .findFirst()
                .orElse(null);

        if (voucher == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Voucher này hiện không khả dụng hoặc đã hết hạn"));
        }

        // Kiểm tra xem user đã có voucher này chưa
        Optional<VoucherItem> itemOpt = voucherItemRepository.findByAccountAndVoucherPercent(user, voucher);
        VoucherItem voucherItem;
        if (itemOpt.isPresent()) {
            voucherItem = itemOpt.get();
            if (voucherItem.getStatus() == 1) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã sử dụng voucher này rồi"));
            }
        } else {
            // Nếu chưa có, thêm mới vào VoucherItem cho user
            if (voucher.getQuantity() != null && voucher.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Voucher này đã hết lượt sử dụng"));
            }

            // Giảm số lượng voucher tổng
            if (voucher.getQuantity() != null) {
                voucher.setQuantity(voucher.getQuantity() - 1);
                voucherPercentRepository.save(voucher);
            }

            voucherItem = new VoucherItem();
            voucherItem.setAccount(user);
            voucherItem.setVoucherPercent(voucher);
            voucherItem.setStatus(0); // Chưa sử dụng
            voucherItem = voucherItemRepository.save(voucherItem);
        }

        // Trả về thông tin voucher để frontend áp dụng
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", voucherItem.getId());
        response.put("name", voucher.getName());
        response.put("percent", voucher.getPercentVoucher());
        response.put("minPrice", voucher.getMinPrice());
        response.put("maxPrice", voucher.getMaxPrice());
        response.put("type", voucher.getVoucherType());

        return ResponseEntity.ok(response);
    }
}
