package com.techone.service;

import com.techone.model.Account;
import com.techone.model.Order;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface OrderService {
    Order createOrder(Account user, List<Integer> itemIds, Integer addressId, String paymentMethod, Integer voucherId,
            String note,
            HttpSession session) throws Exception;

    List<Integer> cancelOrder(Integer orderId);

    void cancelExpiredOrders();
}
