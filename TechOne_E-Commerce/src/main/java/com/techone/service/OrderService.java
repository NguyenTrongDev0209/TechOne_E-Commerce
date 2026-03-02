package com.techone.service;

import com.techone.model.Account;
import com.techone.model.Order;
import jakarta.servlet.http.HttpSession;

public interface OrderService {
    Order createOrder(Account user, Integer addressId, String paymentMethod, Integer voucherId, String note,
            HttpSession session) throws Exception;

    void cancelExpiredOrders();
}
