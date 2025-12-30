package com.example.WorkWite_Repo_BE.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {
    private Long id;
    private String txnRef;
    private Long amount;
    private String status;
    private String responseCode;
    private String orderInfo;
    private String transactionNo;
    private String bankCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
