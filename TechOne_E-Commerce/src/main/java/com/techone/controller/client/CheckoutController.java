package com.techone.controller.client;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techone.model.Account;
import com.techone.model.Address;
import com.techone.model.Cart;
import com.techone.model.CartItem;
import com.techone.model.Order;
import com.techone.model.OrderDetail;
import com.techone.model.Shipment;
import com.techone.model.Variant;
import com.techone.model.VoucherItem;
import com.techone.model.VoucherPercent;
import com.techone.repository.AddressRepository;
import com.techone.repository.CartItemRepository;
import com.techone.repository.CartRepository;
import com.techone.repository.OrderDetailRepository;
import com.techone.repository.OrderRepository;
import com.techone.repository.ShipmentRepository;
import com.techone.repository.VariantRepository;
import com.techone.repository.VoucherItemRepository;
import com.techone.service.ShippingFeeService;

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
	private OrderDetailRepository orderDetailRepository;

	@Autowired
	private ShipmentRepository shipmentRepository;

	@Autowired
	private ShippingFeeService shippingFeeService;

	@Autowired
	private VariantRepository variantRepository;

	@Autowired
	private PayOS payOS;

	@GetMapping("/checkout")
	public String showCheckout(@RequestParam(value = "itemIds", required = false) List<Integer> itemIds,
			HttpSession session, Model model) {
		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
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
			HttpSession session,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {

		System.out.println("DEBUG: placeOrder - paymentMethod=" + paymentMethod);
		System.out.println("DEBUG: placeOrder - note=" + note);
		System.out.println("DEBUG: placeOrder - addressId=" + addressId);
		System.out.println("DEBUG: placeOrder - voucherId=" + voucherId);

		Account user = (Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		if (addressId == null) {
			redirectAttributes.addFlashAttribute("error", "Vui lòng chọn địa chỉ giao hàng");
			return "redirect:/checkout";
		}

		Optional<Address> addressOpt = addressRepository.findById(addressId);
		if (addressOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Địa chỉ không tồn tại");
			return "redirect:/checkout";
		}

		Optional<Cart> cartOpt = cartRepository.findByAccountId(user.getId());
		if (cartOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
			return "redirect:/cart";
		}

		List<CartItem> cartItems = cartItemRepository.findByCart(cartOpt.get());
		if (cartItems.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
			return "redirect:/cart";
		}

		// Check Stock
		for (CartItem item : cartItems) {
			Variant variant = item.getVariant();
			if (variant.getStock() == null || variant.getStock() < item.getQuantity()) {
				redirectAttributes.addFlashAttribute("error",
						"Sản phẩm '" + variant.getProduct().getName() + " [" + variant.getVariantName()
								+ "]' đã hết hàng hoặc không đủ số lượng");
				return "redirect:/checkout";
			}
		}

		// 1. Create Order
		Order order = new Order();
		order.setCreateAt(LocalDateTime.now());
		order.setStatus(0); // 0: Pending/Processing
		order.setNote(note);
		order.setAccount(user);
		order = orderRepository.save(order);

		// 1.5 Create Shipment
		Shipment shipment = new Shipment();
		shipment.setOrder(order);
		shipment.setAddress(addressOpt.get());
		shipment.setStatus(0); // 0: Pending/Processing
		shipment.setCreateAt(LocalDateTime.now());
		shipmentRepository.save(shipment);

		// 2. Calculate Shipping Fee
		Integer shippingFee = 0;
		try {
			shippingFee = shippingFeeService.calculateShippingFee(null,
					addressOpt.get().getWard().getDistrict().getId(),
					addressOpt.get().getWard().getCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		order.setShippingFee(shippingFee.doubleValue());

		// 3. Create OrderDetails & Calculate Total
		long productTotal = 0;
		List<PaymentLinkItem> payOSItems = new ArrayList<>();

		for (CartItem item : cartItems) {
			OrderDetail detail = new OrderDetail();
			detail.setOrder(order);
			detail.setVariant(item.getVariant());
			detail.setQuantity(item.getQuantity());

			double pricePerItem = item.getVariant().getPrice() * (1 - item.getVariant().getDiscount() / 100.0);
			detail.setUnitPrice(pricePerItem);
			orderDetailRepository.save(detail);

			// Reduce Stock
			Variant variant = item.getVariant();
			variant.setStock(variant.getStock() - item.getQuantity());
			variantRepository.save(variant);

			productTotal += (long) (pricePerItem * item.getQuantity());

			payOSItems.add(PaymentLinkItem.builder()
					.name(item.getVariant().getProduct().getName())
					.quantity(item.getQuantity())
					.price((long) pricePerItem)
					.build());
		}

		long finalTotalAmount = productTotal + shippingFee;

		// 3.5 Apply Voucher
		double voucherDiscount = 0;
		if (voucherId != null) {
			Optional<VoucherItem> vItemOpt = voucherItemRepository.findById(voucherId);
			if (vItemOpt.isPresent()) {
				VoucherItem vItem = vItemOpt.get();
				if (vItem.getAccount().getId().equals(user.getId()) && vItem.getStatus() == 0) {
					VoucherPercent vPercent = vItem.getVoucherPercent();
					if (productTotal >= vPercent.getMinPrice()) {
						if (vPercent.getVoucherType() != null && vPercent.getVoucherType() == true) {
							voucherDiscount = productTotal * (vPercent.getPercentVoucher() / 100.0);
						} else {
							voucherDiscount = shippingFee * (vPercent.getPercentVoucher() / 100.0);
						}

						if (vPercent.getMaxPrice() != null && voucherDiscount > vPercent.getMaxPrice()) {
							voucherDiscount = vPercent.getMaxPrice();
						}
						vItem.setStatus(1); // Mark as used
						voucherItemRepository.save(vItem);
					}
				}
			}
		}
		order.setVoucherDiscount(voucherDiscount);
		finalTotalAmount -= (long) voucherDiscount;

		order.setTotalAmount((double) finalTotalAmount);
		orderRepository.save(order);

		// 4. Handle Payment
		if ("QR".equals(paymentMethod)) {
			try {
				String baseUrl = getBaseUrl(request);
				String returnUrl = baseUrl + "/payment/success";
				String cancelUrl = baseUrl + "/payment/cancel";

				long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3));
				order.setOrderCode(orderCode);
				orderRepository.save(order);

				CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
						.orderCode(orderCode)
						.amount(finalTotalAmount)
						.description("Thanh toan don hang #" + order.getId())
						.returnUrl(returnUrl)
						.cancelUrl(cancelUrl)
						.items(payOSItems)
						.build();

				CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

				// Pass PayOS data to the next request via FlashAttributes
				redirectAttributes.addFlashAttribute("qrCode", data.getQrCode());
				redirectAttributes.addFlashAttribute("accountName", data.getAccountName());
				redirectAttributes.addFlashAttribute("accountNumber", data.getAccountNumber());
				redirectAttributes.addFlashAttribute("bin", data.getBin());
				redirectAttributes.addFlashAttribute("checkoutUrl", data.getCheckoutUrl());
				redirectAttributes.addFlashAttribute("amount", data.getAmount());
				redirectAttributes.addFlashAttribute("description", data.getDescription());

				return "redirect:/checkout/payos?orderId=" + order.getId();
			} catch (Exception e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute("error", "Lỗi tạo liên kết thanh toán PayOS: " + e.getMessage());
				return "redirect:/checkout";
			}
		} else {
			// COD
			cartItemRepository.deleteAll(cartItems);
			session.setAttribute("cartCount", 0);
			return "redirect:/checkout/order-success";
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

		// If QR data is already in model (from FlashAttributes), we are good
		if (model.containsAttribute("qrCode") || model.containsAttribute("qrUrl")) {
			return "views/client/payos";
		}

		try {
			// Fetch actual payment info from PayOS API as fallback
			vn.payos.model.v2.paymentRequests.PaymentLink paymentLink = payOS.paymentRequests()
					.get(order.getOrderCode());

			model.addAttribute("amount", paymentLink.getAmount());

			// Try to get QR code if available in PaymentLink
			String qrCodeValue = null;
			try {
				qrCodeValue = (String) paymentLink.getClass().getMethod("getQrCode").invoke(paymentLink);
			} catch (Exception e_qr) {
			}

			if (qrCodeValue != null && !qrCodeValue.isEmpty()) {
				model.addAttribute("qrCode", qrCodeValue);
			} else {
				// Fallback to manual QR URL if qrcode data is missing
				String qrUrlFallback = String
						.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s&accountName=%s",
								"MB", "970422", order.getTotalAmount().longValue(),
								"Thanh toan don hang " + order.getId(),
								"LE QUOC MINH")
						.replace(" ", "%20");
				model.addAttribute("qrUrl", qrUrlFallback);
			}

			try {
				model.addAttribute("checkoutUrl",
						paymentLink.getClass().getMethod("getCheckoutUrl").invoke(paymentLink));
			} catch (Exception e_url) {
			}

			try {
				model.addAttribute("accountNumber",
						paymentLink.getClass().getMethod("getAccountNumber").invoke(paymentLink));
				model.addAttribute("accountName",
						paymentLink.getClass().getMethod("getAccountName").invoke(paymentLink));
				model.addAttribute("bin", paymentLink.getClass().getMethod("getBin").invoke(paymentLink));
				model.addAttribute("bankName", "Ngân hàng liên kết");
			} catch (Exception e_info) {
				model.addAttribute("bankName", "MB Bank");
				model.addAttribute("accountNumber", "970422");
				model.addAttribute("accountName", "LE QUOC MINH");
			}

			try {
				model.addAttribute("description",
						paymentLink.getClass().getMethod("getDescription").invoke(paymentLink));
			} catch (Exception e_desc) {
				model.addAttribute("description", "Thanh toan don hang #" + order.getId());
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Manual fallback
			model.addAttribute("bankName", "MB Bank");
			model.addAttribute("accountNumber", "970422");
			model.addAttribute("accountName", "LE QUOC MINH");
			model.addAttribute("amount", order.getTotalAmount());
			model.addAttribute("description", "TECHONE ORDER " + order.getId());

			String qrUrlFallback = String
					.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s&accountName=%s",
							"MB", "970422", order.getTotalAmount().longValue(), "Thanh toan don hang " + order.getId(),
							"LE QUOC MINH")
					.replace(" ", "%20");
			model.addAttribute("qrUrl", qrUrlFallback);
		}

		return "views/client/payos";
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
