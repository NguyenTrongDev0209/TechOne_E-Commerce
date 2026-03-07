package com.techone.controller.client;

import com.techone.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(value = "orderCode", required = false) String orderCode, Model model) {
        if (orderCode != null) {
            try {
                paymentService.verifyAndUpdateOrder(Long.parseLong(orderCode));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("orderCode", orderCode);
        return "views/client/order-success";
    }

    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam(value = "orderCode", required = false) String orderCode, Model model) {
        model.addAttribute("orderCode", orderCode);
        return "views/client/order-cancel";
    }
}
