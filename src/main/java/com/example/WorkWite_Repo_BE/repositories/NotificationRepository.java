package com.example.WorkWite_Repo_BE.repositories;

import com.example.WorkWite_Repo_BE.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // ✅ Fix N+1 Query: Dùng JOIN FETCH
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.applicant WHERE n.user.id = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // ✅ Pagination cho unread
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.applicant WHERE n.user.id = :userId AND n.isRead = false AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Đếm số thông báo chưa đọc
    Long countByUserIdAndIsReadFalseAndDeletedAtIsNull(Long userId);
    
    // ✅ Authorization: Find với check ownership
    @Query("SELECT n FROM Notification n WHERE n.id = :notificationId AND n.user.id = :userId AND n.deletedAt IS NULL")
    Optional<Notification> findByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
    
    // Đánh dấu thông báo đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.user.id = :userId AND n.deletedAt IS NULL")
    void markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
    
    // Đánh dấu tất cả thông báo là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false AND n.deletedAt IS NULL")
    void markAllAsRead(@Param("userId") Long userId);    
    // ✎️ Tìm notification theo applicant và user (cho candidate)
    @Query("SELECT n FROM Notification n WHERE n.applicant.id = :applicantId AND n.user.id = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Optional<Notification> findByApplicantIdAndUserId(@Param("applicantId") Long applicantId, @Param("userId") Long userId);}
