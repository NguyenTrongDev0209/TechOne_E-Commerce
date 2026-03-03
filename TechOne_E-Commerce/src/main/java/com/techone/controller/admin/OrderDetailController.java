package com.techone.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.techone.model.Order;
import com.techone.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OrderDetailController {

	private final OrderRepository orderRepository;

	@GetMapping("/admin/order-list/order-detail")
	public String orderDetail(@RequestParam("id") Integer id, Model model) {
		Order order = orderRepository.findById(id).orElse(null);
		if (order == null) {
			return "redirect:/admin/order-list";
		}
		model.addAttribute("order", order);
		return "views/admin/order-detail";
	}

	@PostMapping("/admin/order-list/update-status")
	public String updateStatus(@RequestParam("id") Integer id, @RequestParam("status") Integer status) {
		Order order = orderRepository.findById(id).orElse(null);
		if (order != null) {
			order.setStatus(status);
			orderRepository.save(order);

			// If status is 4 (Shipping), simulate 10s then update to 5 (Waiting for User
			// Confirmation)
			if (status == 4) {
				simulateShipping(id);
			}
		}
		return "redirect:/admin/order-list/order-detail?id=" + id;
	}

	@org.springframework.scheduling.annotation.Async
	public void simulateShipping(Integer orderId) {
		try {
			Thread.sleep(10000); // 10 seconds
			Order order = orderRepository.findById(orderId).orElse(null);
			if (order != null && order.getStatus() == 4) {
				order.setStatus(5);
				orderRepository.save(order);
				System.out.println("DEBUG: Order #" + orderId + " auto-updated from 4 to 5 after 10s simulation.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
