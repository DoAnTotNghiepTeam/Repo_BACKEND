package com.example.WorkWite_Repo_BE.entities;

import com.example.WorkWite_Repo_BE.enums.ApplicationStatus;
import com.example.WorkWite_Repo_BE.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id,created_at"),
    @Index(name = "idx_user_read", columnList = "user_id,is_read")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Người nhận thông báo
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;  // Liên kết đến đơn ứng tuyển
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;  // Loại thông báo
    
    @Column(name = "title", nullable = false)
    private String title;  // Tiêu đề thông báo
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;  // Nội dung thông báo
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status;  // Trạng thái liên quan (CV_PASSED, INTERVIEW, etc.)
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;  // Đã đọc chưa
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // Soft delete
    
    @Column(name = "job_title")
    private String jobTitle;  // Tên công việc
    
    @Column(name = "company_name")
    private String companyName;  // Tên công ty
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
