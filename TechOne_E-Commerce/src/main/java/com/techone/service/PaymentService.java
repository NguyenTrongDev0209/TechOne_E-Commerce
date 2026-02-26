package com.techone.service;

import com.techone.model.Order;
import com.techone.model.Transaction;
import com.techone.repository.OrderRepository;
import com.techone.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayOS payOS;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void processWebhook(Object body) throws Exception {
        // 1. Verify Webhook from PayOS
        WebhookData data = payOS.webhooks().verify(body);

        // 2. Map WebhookData to Transaction
        updateOrderAndSaveTransaction(
                data.getOrderCode(),
                (long) data.getAmount(),
                data.getReference(),
                data.getPaymentLinkId(),
                "PAID",
                data.getAccountNumber(),
                data.getTransactionDateTime(),
                data.getCurrency(),
                data.getCounterAccountNumber(),
                data.getCounterAccountBankId(),
                data.getDescription());
    }

    @Transactional
    public void verifyAndUpdateOrder(Long orderCode) throws Exception {
        // 1. Check if transaction already exists to avoid double processing
        Optional<Transaction> existingTransaction = transactionRepository.findByOrderCode(orderCode);
        if (existingTransaction.isPresent()) {
            return; // Already processed
        }

        // 2. Fetch payment info from PayOS API
        PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);

        // 3. If status is PAID, update order and save transaction
        if ("PAID".equals(paymentLink.getStatus())) {
            updateOrderAndSaveTransaction(
                    orderCode,
                    (long) paymentLink.getAmount(),
                    "MANUAL_VERIFY",
                    paymentLink.getId(),
                    "PAID",
                    null, // accountNumber might not be in PaymentLink
                    LocalDateTime.now().toString(),
                    "VND",
                    null,
                    null,
                    "Xác minh thủ công từ Redirect");
        }
    }

    private void updateOrderAndSaveTransaction(Long orderCode, Long amount, String reference,
            String paymentLinkId, String status, String accountNumber,
            String txDateTime, String currency, String senderAccount,
            String senderBankId, String description) {
        // 1. Find the order
        Integer orderId = orderCode.intValue();
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Update order status if not already paid
            if (order.getStatus() != 1) {
                order.setStatus(1); // 1: Paid/Success
                orderRepository.save(order);
            }

            // 2. Clear duplicated transaction if any
            Optional<Transaction> existing = transactionRepository.findByOrderCode(orderCode);
            if (existing.isPresent()) {
                return; // Don't save again
            }

            // 3. Create Transaction record
            Transaction transaction = new Transaction();
            transaction.setOrder(order);
            transaction.setAmount(amount.doubleValue());
            transaction.setPaymentMethod("PAYOS");
            transaction.setTransactionType("PAYMENT");
            transaction.setStatus(1); // Success
            transaction.setOrderCode(orderCode);
            transaction.setReference(reference);
            transaction.setPaymentLinkId(paymentLinkId);
            transaction.setLog(status); // Using log to store PayOS status string
            transaction.setCreateAt(LocalDateTime.now());

            // PayOS detailed fields
            transaction.setAccountNumber(accountNumber);
            transaction.setTransactionDateTime(txDateTime);
            transaction.setCurrency(currency);
            transaction.setSenderAccountNumber(senderAccount);
            transaction.setSenderBankId(senderBankId);
            transaction.setDescription(description);

            transactionRepository.save(transaction);
        }
    }
}
