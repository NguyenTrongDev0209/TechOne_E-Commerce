package com.techone.controller.client;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techone.model.Account;
import com.techone.model.Address;
import com.techone.model.Cart;
import com.techone.model.CartItem;
import com.techone.model.Order;
import com.techone.model.OrderDetail;
import com.techone.model.VoucherItem;
import com.techone.repository.AddressRepository;
import com.techone.repository.CartItemRepository;
import com.techone.repository.CartRepository;
import com.techone.repository.OrderRepository;
import com.techone.repository.VoucherItemRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Controller
public class CheckoutController {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private VoucherItemRepository voucherItemRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private PayOS payOS;

	@Autowired
	private com.techone.service.OrderService orderService;

	@GetMapping("/checkout")
	public String showCheckout(@RequestParam(value = "itemIds", required = false) List<Integer> itemIds,
			HttpSession session, Model model) {
		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		// Check for pending order (Status = 1)
		Optional<Order> pendingOrder = orderRepository.findFirstByAccountIdAndStatus(user.getId(), 1);
		if (pendingOrder.isPresent()) {
			return "redirect:/checkout/payos?orderId=" + pendingOrder.get().getId();
		}

		// Get Address Data
		List<Address> addresses = addressRepository.findByAccountIdOrderByStatusDesc(user.getId());
		model.addAttribute("addresses", addresses);
		System.out.println("DEBUG: showCheckout - addresses count=" + (addresses != null ? addresses.size() : 0));

		// Get default address setup
		Address defaultAddress = addresses.stream()
				.filter(Address::getStatus)
				.findFirst()
				.orElse(addresses.isEmpty() ? null : addresses.get(0));
		model.addAttribute("defaultAddress", defaultAddress);
		System.out.println(
				"DEBUG: showCheckout - defaultAddress=" + (defaultAddress != null ? defaultAddress.getId() : "NULL"));

		// Get Cart Data
		List<CartItem> cartItems = new ArrayList<>();
		Optional<Cart> cartOpt = cartRepository.findByAccountId(user.getId());
		if (cartOpt.isPresent()) {
			cartItems = cartItemRepository.findByCart(cartOpt.get());
			if (itemIds != null && !itemIds.isEmpty()) {
				cartItems = cartItems.stream()
						.filter(item -> itemIds.contains(item.getId()))
						.toList();
			}
		}

		if (cartItems.isEmpty()) {
			return "redirect:/cart";
		}

		model.addAttribute("cartItems", cartItems);

		// Get Voucher Data
		LocalDateTime now = LocalDateTime.now();
		List<VoucherItem> validVouchers = voucherItemRepository.findByAccount(user).stream()
				.filter(v -> v.getStatus() != null && v.getStatus() == 0) // 0: Unused
				.filter(v -> v.getVoucherPercent().getStatus() != null && v.getVoucherPercent().getStatus() == 1) // 1:
																													// Active
																													// in
																													// system
				.filter(v -> !now.isBefore(v.getVoucherPercent().getActiveDay()))
				.filter(v -> !now.isAfter(v.getVoucherPercent().getEndAt()))
				.toList();
		model.addAttribute("vouchers", validVouchers);

		return "views/client/checkout";
	}

	@PostMapping("/checkout/place-order")
	public String placeOrder(@RequestParam("paymentMethod") String paymentMethod,
			@RequestParam(value = "note", required = false) String note,
			@RequestParam(value = "addressId", required = false) Integer addressId,
			@RequestParam(value = "voucherId", required = false) Integer voucherId,
			@RequestParam(value = "itemIds", required = false) List<Integer> itemIds,
			HttpSession session,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {

		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		try {
			// Call OrderService to handle logic (Validation, Order/Shipment creation,
			// Stock, Voucher)
			Order order = orderService.createOrder(user, itemIds, addressId, paymentMethod, voucherId, note, session);

			System.out.println("DEBUG: CheckoutController - Order status after createOrder: " + order.getStatus());

			// Handle Online Payment (QR)
			if ("QR".equals(paymentMethod)) {
				try {
					String baseUrl = getBaseUrl(request);
					String returnUrl = baseUrl + "/payment/success";
					String cancelUrl = baseUrl + "/payment/cancel";

					long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3));
					order.setOrderCode(orderCode);
					orderRepository.save(order);

					System.out.println("DEBUG: CheckoutController - Order status after save(order) with orderCode: "
							+ order.getStatus());

					List<PaymentLinkItem> payOSItems = new ArrayList<>();
					if (order.getOrderDetail() != null) {
						for (OrderDetail detail : order.getOrderDetail()) {
							payOSItems.add(PaymentLinkItem.builder()
									.name(detail.getVariant().getProduct().getName())
									.quantity(detail.getQuantity())
									.price(detail.getUnitPrice().longValue())
									.build());
						}
					}

					CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
							.orderCode(orderCode)
							.amount(order.getTotalAmount().longValue())
							.description("Thanh toan don hang #" + order.getId())
							.returnUrl(returnUrl)
							.cancelUrl(cancelUrl)
							.items(payOSItems)
							.build();

					CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

					// Lưu thông tin thanh toán vào Order để dùng lại khi quay lại trang
					order.setQrCode(data.getQrCode());
					order.setCheckoutUrl(data.getCheckoutUrl());
					order.setAccountName(data.getAccountName());
					order.setAccountNumber(data.getAccountNumber());
					order.setBin(data.getBin());
					orderRepository.save(order);

					// Truyền dữ liệu qua FlashAttributes cho lần load đầu tiên
					redirectAttributes.addFlashAttribute("qrCode", data.getQrCode());
					redirectAttributes.addFlashAttribute("accountName", data.getAccountName());
					redirectAttributes.addFlashAttribute("accountNumber", data.getAccountNumber());
					redirectAttributes.addFlashAttribute("bin", data.getBin());
					redirectAttributes.addFlashAttribute("bankName", "Ngân hàng MB Bank");
					redirectAttributes.addFlashAttribute("checkoutUrl", data.getCheckoutUrl());
					redirectAttributes.addFlashAttribute("amount", data.getAmount());
					redirectAttributes.addFlashAttribute("description", data.getDescription());

					return "redirect:/checkout/payos?orderId=" + order.getId();
				} catch (Exception e) {
					e.printStackTrace();
					redirectAttributes.addFlashAttribute("error",
							"Lỗi tạo liên kết thanh toán PayOS: " + e.getMessage());
					return "redirect:/checkout";
				}
			} else {
				// COD Success
				return "redirect:/checkout/order-success";
			}
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/checkout";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi trong quá trình đặt hàng");
			return "redirect:/checkout";
		}
	}

	@GetMapping("/checkout/payos")
	public String showPayos(@RequestParam("orderId") Integer orderId, HttpSession session, Model model) {
		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		Optional<Order> orderOpt = orderRepository.findById(orderId);
		if (orderOpt.isEmpty() || !orderOpt.get().getAccount().getId().equals(user.getId())) {
			return "redirect:/";
		}

		Order order = orderOpt.get();
		model.addAttribute("order", order);
		model.addAttribute("amount", order.getTotalAmount());
		model.addAttribute("description", "Thanh toan don hang #" + order.getId());

		// Ưu tiên 1: dữ liệu từ FlashAttributes (ngay sau khi tạo đơn mới)
		if (model.containsAttribute("qrCode")) {
			return "views/client/payos";
		}

		// Ưu tiên 2: dữ liệu đã lưu trong Order entity (khi quay lại trang)
		if (order.getQrCode() != null && !order.getQrCode().isEmpty()) {
			System.out.println("DEBUG: showPayos - Loading qrCode from Order entity for orderId=" + orderId);
			model.addAttribute("qrCode", order.getQrCode());
			if (order.getCheckoutUrl() != null) {
				model.addAttribute("checkoutUrl", order.getCheckoutUrl());
			}
			if (order.getAccountName() != null) {
				model.addAttribute("accountName", order.getAccountName());
			}
			if (order.getAccountNumber() != null) {
				model.addAttribute("accountNumber", order.getAccountNumber());
			}
			if (order.getBin() != null) {
				model.addAttribute("bin", order.getBin());
				model.addAttribute("bankName", "Ngân hàng liên kết PayOS");
			}
			return "views/client/payos";
		}

		// Ưu tiên 3: Gọi PayOS API để lấy số tiền (trường hợp đơn cũ chưa lưu qrCode)
		// Lưu ý: PaymentLink SDK không trả về qrCode/checkoutUrl, chỉ có trạng thái đơn
		// hàng
		if (order.getOrderCode() != null) {
			try {
				vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests()
						.get(order.getOrderCode());
				model.addAttribute("amount", paymentLink.getAmount());
				System.out.println("DEBUG: showPayos - Loaded payment link from PayOS API for orderId=" + orderId);
			} catch (Exception e) {
				System.out.println("DEBUG: showPayos - Could not fetch PayOS link: " + e.getMessage());
			}
		}

		return "views/client/payos";
	}

	@GetMapping("/checkout/cancel/{orderId}")
	public String cancelOrder(@PathVariable("orderId") Integer orderId, HttpSession session) {
		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		// Kiểm tra quyền sở hữu đơn hàng trước khi cho phép hủy
		Optional<Order> orderOpt = orderRepository.findById(orderId);
		if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
				|| !orderOpt.get().getAccount().getId().equals(user.getId())) {
			System.out.println(
					"DEBUG: Unauthorized cancel attempt for orderId=" + orderId + " by userId=" + user.getId());
			return "redirect:/";
		}

		List<Integer> restoredItemIds = orderService.cancelOrder(orderId);
		cartRepository.findByAccountId(user.getId()).ifPresent(cart -> {
			int cartCount = cartItemRepository.findActiveItemsByCart(cart).size();
			session.setAttribute("cartCount", cartCount);
		});

		// Xây dựng URL chuyển hướng kèm theo các itemIds đã khôi phục
		StringBuilder redirectUrl = new StringBuilder("redirect:/checkout");
		if (restoredItemIds != null && !restoredItemIds.isEmpty()) {
			redirectUrl.append("?");
			for (int i = 0; i < restoredItemIds.size(); i++) {
				if (i > 0)
					redirectUrl.append("&");
				redirectUrl.append("itemIds=").append(restoredItemIds.get(i));
			}
		}

		return redirectUrl.toString();
	}

	private String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		String url = scheme + "://" + serverName;
		if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
			url += ":" + serverPort;
		}
		url += contextPath;
		return url;
	}
}
