package com.techone.domain.user.controller.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Address;
import com.techone.domain.user.entity.Favourite;
import com.techone.domain.order.entity.Order;
import com.techone.domain.user.repository.AddressRepository;
import com.techone.domain.user.repository.FavouriteRepository;
import com.techone.domain.order.repository.OrderRepository;
import com.techone.domain.promotion.repository.VoucherItemRepository;
import com.techone.common.utils.SessionUtils;

@Controller
public class AccountController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VoucherItemRepository voucherItemRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/account")
    public String showAccount(Model model) {
        Account user = SessionUtils.get("user");
        if (user == null) {
            return "redirect:/login";
        }

        Integer userId = user.getId();
        long orderCount = orderRepository.countByAccountId(userId);
        model.addAttribute("orderCount", orderCount);

        long voucherCount = voucherItemRepository.countByAccountIdAndStatus(userId, 0);
        model.addAttribute("voucherCount", voucherCount);

        List<Order> allOrders = orderRepository.findByAccountIdOrderByCreateAtDesc(userId);
        List<Order> recentOrders = allOrders.stream().limit(5).toList();
        model.addAttribute("recentOrders", recentOrders);

        List<Favourite> favouriteProducts = favouriteRepository.findByAccount(user);
        model.addAttribute("favouriteProducts", favouriteProducts);

        Address defaultAddress = addressRepository.findByAccountIdAndStatusTrue(userId).orElse(null);
        model.addAttribute("defaultAddress", defaultAddress);

        return "views/client/account";
    }
}


