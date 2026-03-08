package com.techone.domain.order.service;

import com.techone.domain.order.entity.Order;
import com.techone.domain.order.entity.Transaction;
import com.techone.domain.promotion.entity.VoucherItem;
import com.techone.domain.order.repository.OrderRepository;
import com.techone.domain.order.repository.TransactionRepository;
import com.techone.domain.promotion.repository.VoucherItemRepository;
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
    private final VoucherItemRepository voucherItemRepository;

    @Transactional
    public void processWebhook(Object body) throws Exception {
        WebhookData data = payOS.webhooks().verify(body);
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
        Optional<Transaction> existingTransaction = transactionRepository.findByOrderCode(orderCode);
        if (existingTransaction.isPresent())
            return;

        PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
        if ("PAID".equals(paymentLink.getStatus().toString())) {
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
        java.util.Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getStatus() != 2) {
                order.setStatus(2);
                orderRepository.save(order);
                if (order.getAppliedVoucher() != null && order.getAppliedVoucher().getStatus() == 0) {
                    VoucherItem voucher = order.getAppliedVoucher();
                    voucher.setStatus(1);
                    voucherItemRepository.save(voucher);
                }
                try {
                    orderEmailService.sendOrderInvoice(order);
                } catch (Exception e) {
                }
            }
            Optional<Transaction> existing = transactionRepository.findByOrderCode(orderCode);
            if (existing.isPresent())
                return;

            Transaction transaction = new Transaction();
            transaction.setOrder(order);
            transaction.setAmount(amount.doubleValue());
            transaction.setPaymentMethod("PAYOS");
            transaction.setTransactionType("PAYMENT");
            transaction.setStatus(1);
            transaction.setOrderCode(orderCode);
            transaction.setReference(reference);
            transaction.setPaymentLinkId(paymentLinkId);
            transaction.setLog(status);
            transaction.setCreateAt(LocalDateTime.now());
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

