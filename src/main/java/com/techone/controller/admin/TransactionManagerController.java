package com.techone.controller.admin;

import com.techone.domain.order.entity.Transaction;
import com.techone.domain.order.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TransactionManagerController {

        private final TransactionRepository transactionRepository;

        @GetMapping("/admin/transaction-manager")
        public String transactionManager(Model model) {
                List<Transaction> transactions = transactionRepository.findAllByOrderByCreateAtDesc();
                model.addAttribute("transactions", transactions);

                // Simple stats calculation
                double totalRevenue = transactions.stream()
                                .filter(t -> t.getStatus() != null && t.getStatus() == 1) // 1: Success/Paid
                                .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                                .sum();
                long successCount = transactions.stream()
                                .filter(t -> t.getStatus() != null && t.getStatus() == 1)
                                .count();
                long pendingCount = transactions.stream()
                                .filter(t -> t.getStatus() == null || t.getStatus() != 1)
                                .count();

                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("successCount", successCount);
                model.addAttribute("pendingCount", pendingCount);

                return "views/admin/transaction-manager";
        }
}

