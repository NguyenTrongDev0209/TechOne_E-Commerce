package com.techone.controller.api;

import com.techone.model.Order;
import com.techone.repository.OrderRepository;
import com.techone.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @PostMapping("/payos_transfer_handler")
    public Object payosTransferHandler(@RequestBody Object body) {
        try {
            paymentService.processWebhook(body);
            return java.util.Map.of("success", true, "message", "Webhook delivered");
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/check-status/{orderId}")
    public Object getPaymentStatus(@PathVariable("orderId") int orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Return a simple object matching the expected "status" in JS
            return java.util.Map.of("status", order.getStatus() == 1 ? "PAID" : "PENDING");
        }
        return java.util.Map.of("status", "NOT_FOUND");
    }
}
