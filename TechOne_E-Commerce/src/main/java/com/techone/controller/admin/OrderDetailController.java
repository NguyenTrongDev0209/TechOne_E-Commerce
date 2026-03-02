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
		}
		return "redirect:/admin/order-list/order-detail?id=" + id;
	}
}
