package com.example.WorkWite_Repo_BE.dtos.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private String notificationType;  // APPLY_SUCCESS, STATUS_UPDATE_PASSED, etc.
    private String title;
    private String message;
    private String status;  // PENDING, CV_PASSED, etc.
    private Boolean isRead;
    private LocalDateTime createdAt;
    private Long applicantId;
    private String jobTitle;
    private String companyName;
}
