package com.techone.domain.order.entity;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @PositiveOrZero(message = "Tổng tiền không thể âm")
    public Double amount;

    @Column(columnDefinition = "varchar(255)")
    public String paymentMethod;

    public Integer status;

    @Column(columnDefinition = "varchar(255)")
    public String transactionType;

    public Integer refId;

    @PastOrPresent(message = "Ngày tạo giao dịch không thể ở tương lai")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    public LocalDateTime createAt = LocalDateTime.now();

    @Column(columnDefinition = "varchar(255)")
    public String log;

    // PayOS specific fields
    private Long orderCode;
    private String description;
    private String accountNumber;
    private String reference;
    private String transactionDateTime;
    private String currency;
    private String paymentLinkId;
    private String senderAccountNumber;
    private String senderBankId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;
}
