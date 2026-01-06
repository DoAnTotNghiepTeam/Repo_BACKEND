package com.example.WorkWite_Repo_BE.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String txnRef; // Mã giao dịch (vnp_TxnRef)

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount; // Số tiền (VND)

    @Column(nullable = false, length = 20)
    private String status; // PENDING, SUCCESS, FAILED

    @Column(length = 10)
    private String responseCode; // vnp_ResponseCode từ VNPay

    @Column(length = 500)
    private String orderInfo; // Thông tin đơn hàng

    @Column(columnDefinition = "TEXT")
    private String vnpayData; // JSON data từ VNPay callback

    @Column(length = 100)
    private String transactionNo; // Mã giao dịch tại VNPay (vnp_TransactionNo)

    @Column(length = 50)
    private String bankCode; // Mã ngân hàng

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String ipAddress; // IP của người thanh toán
}
