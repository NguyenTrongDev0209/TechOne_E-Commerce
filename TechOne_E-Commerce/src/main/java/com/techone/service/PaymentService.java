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
    private final OrderEmailService orderEmailService;

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
        if ("PAID".equals(paymentLink.getStatus().toString())) {

            // Try to extract transaction details from paymentLink via reflection
            // PayOS PaymentLink usually contains a list of transactions
            Long amount = (long) paymentLink.getAmount();
            String reference = "MANUAL_VERIFY";
            String accountNumber = null;
            String senderAccount = null;
            String senderBankId = null;
            String description = "Xác minh thủ công từ Redirect/Polling";
            String txDateTime = LocalDateTime.now().toString();

            try {
                java.util.List<?> transactions = (java.util.List<?>) paymentLink.getClass().getMethod("getTransactions")
                        .invoke(paymentLink);
                if (transactions != null && !transactions.isEmpty()) {
                    Object lastTx = transactions.get(transactions.size() - 1);

                    try {
                        reference = (String) lastTx.getClass().getMethod("getReference").invoke(lastTx);
                    } catch (Exception e) {
                    }
                    try {
                        accountNumber = (String) lastTx.getClass().getMethod("getAccountNumber").invoke(lastTx);
                    } catch (Exception e) {
                    }
                    try {
                        senderAccount = (String) lastTx.getClass().getMethod("getCounterAccountNumber").invoke(lastTx);
                    } catch (Exception e) {
                    }
                    try {
                        senderBankId = (String) lastTx.getClass().getMethod("getCounterAccountBankId").invoke(lastTx);
                    } catch (Exception e) {
                    }
                    try {
                        description = (String) lastTx.getClass().getMethod("getDescription").invoke(lastTx);
                    } catch (Exception e) {
                    }
                    try {
                        txDateTime = (String) lastTx.getClass().getMethod("getTransactionDateTime").invoke(lastTx);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e_reflect) {
                // Fallback to basic info if transactions list is not accessible
            }

            updateOrderAndSaveTransaction(
                    orderCode,
                    amount,
                    reference,
                    paymentLink.getId(),
                    "PAID",
                    accountNumber,
                    txDateTime,
                    "VND",
                    senderAccount,
                    senderBankId,
                    description);
        }
    }

    private void updateOrderAndSaveTransaction(Long orderCode, Long amount, String reference,
            String paymentLinkId, String status, String accountNumber,
            String txDateTime, String currency, String senderAccount,
            String senderBankId, String description) {
        // 1. Find the order using the unique orderCode
        java.util.Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Update order status if not already paid
            if (order.getStatus() != 2) {
                order.setStatus(2); // 2: Paid/Success
                orderRepository.save(order);

                // Send Invoice Email for Online Payment
                try {
                    orderEmailService.sendOrderInvoice(order);
                } catch (Exception e) {
                    System.err.println("DEBUG: Failed to send Online invoice email: " + e.getMessage());
                }
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
