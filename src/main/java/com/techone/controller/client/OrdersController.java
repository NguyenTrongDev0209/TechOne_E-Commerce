package com.techone.controller.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techone.domain.user.entity.Account;
import com.techone.domain.order.entity.Order;
import com.techone.domain.order.repository.OrderRepository;
import com.techone.common.utils.SessionUtils;

@Controller
public class OrdersController {

	@Autowired
	private OrderRepository orderRepository;

	@GetMapping("/account/orders")
	public String showOders(Model model,
			@RequestParam(name = "status", required = false) String statusStr,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "date", required = false) String dateStr,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		Account user = SessionUtils.get("user");
		if (user == null) {
			return "redirect:/login";
		}

		Integer status = null;
		String statusLabel = "Tất cả trạng thái";
		if (statusStr != null && !statusStr.equals("all")) {
			try {
				status = Integer.parseInt(statusStr);
				statusLabel = getStatusLabel(status);
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		LocalDateTime startDate = null;
		LocalDateTime endDate = null;
		if (dateStr != null && !dateStr.isEmpty()) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
				LocalDate date = LocalDate.parse(dateStr, formatter);
				startDate = date.atStartOfDay();
				endDate = date.atTime(LocalTime.MAX);
			} catch (Exception e) {
				// ignore
			}
		}

		Page<Order> orderPage = orderRepository.findByFilters(
				user.getId(),
				status,
				search,
				startDate,
				endDate,
				PageRequest.of(page, 5));

		model.addAttribute("orders", orderPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", orderPage.getTotalPages());
		model.addAttribute("status", statusStr != null ? statusStr : "all");
		model.addAttribute("statusLabel", statusLabel);
		model.addAttribute("search", search);
		model.addAttribute("date", dateStr);

		return "views/client/orders";
	}

	private String getStatusLabel(int status) {
		switch (status) {
			case 0:
				return "Chưa thanh toán (COD)";
			case 1:
				return "Chờ thanh toán (Online)";
			case 2:
				return "Đã thanh toán";
			case 3:
				return "Đã xác nhận";
			case 4:
				return "Đang giao hàng";
			case 5:
				return "Chờ xác nhận";
			case 6:
				return "Hoàn tất";
			case 7:
				return "Chưa nhận hàng";
			case 8:
				return "Đã hủy";
			default:
				return "Tất cả trạng thái";
		}
	}
}


