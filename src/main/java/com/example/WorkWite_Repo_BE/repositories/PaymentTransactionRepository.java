package com.example.WorkWite_Repo_BE.repositories;

import com.example.WorkWite_Repo_BE.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    // Tìm transaction theo mã giao dịch
    Optional<PaymentTransaction> findByTxnRef(String txnRef);
    
    // Tìm tất cả transaction của user
    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Tìm transaction theo status
    List<PaymentTransaction> findByStatus(String status);
    
    // Check xem txnRef đã tồn tại chưa (để tránh duplicate)
    boolean existsByTxnRef(String txnRef);
}
