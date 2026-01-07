package com.example.WorkWite_Repo_BE.services;

import com.example.WorkWite_Repo_BE.dtos.Notification.NotificationResponseDto;
import com.example.WorkWite_Repo_BE.entities.Applicant;
import com.example.WorkWite_Repo_BE.entities.Notification;
import com.example.WorkWite_Repo_BE.entities.User;
import com.example.WorkWite_Repo_BE.enums.ApplicationStatus;
import com.example.WorkWite_Repo_BE.enums.NotificationType;
import com.example.WorkWite_Repo_BE.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * ✅ Tạo hoặc cập nhật thông báo khi employer cập nhật trạng thái ứng viên
     */
    @Transactional
    public void createStatusUpdateNotification(Applicant applicant, ApplicationStatus newStatus, String note) {
        User candidateUser = applicant.getCandidate().getUser();
        String companyName = applicant.getJobPosting().getEmployer().getCompanyInformation().getCompanyName();
        String jobTitle = applicant.getJobPosting().getTitle();
        
        NotificationType notificationType = mapStatusToNotificationType(newStatus);
        String title = getNotificationTitle(newStatus, companyName);
        String message = getNotificationMessage(newStatus, jobTitle, companyName, note);
        
        // ✅ Tìm notification cũ của applicant này
        Optional<Notification> existingNotification = notificationRepository
                .findByApplicantIdAndUserId(applicant.getId(), candidateUser.getId());
        
        if (existingNotification.isPresent()) {
            // ✅ Nếu đã có notification, UPDATE thông tin
            Notification notification = existingNotification.get();
            notification.setNotificationType(notificationType);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(newStatus);
            notification.setIsRead(false); // Đánh dấu lại chưa đọc để user thấy
            notification.setCreatedAt(LocalDateTime.now()); // Cập nhật thời gian
            
            notificationRepository.save(notification);
            log.info("Đã cập nhật notification {} cho user {} về đơn ứng tuyển {}", 
                     notificationType, candidateUser.getId(), applicant.getId());
        } else {
            // ✅ Nếu chưa có, TẠO MỚI
            Notification notification = Notification.builder()
                    .user(candidateUser)
                    .applicant(applicant)
                    .notificationType(notificationType)
                    .title(title)
                    .message(message)
                    .status(newStatus)
                    .jobTitle(jobTitle)
                    .companyName(companyName)
                    .jobId(applicant.getJobPosting().getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            notificationRepository.save(notification);
            log.info("Đã tạo thông báo {} cho user {} về đơn ứng tuyển {}", 
                     notificationType, candidateUser.getId(), applicant.getId());
        }
    }
    
    /**
     * ✅ Tạo thông báo cho employer khi có ứng viên mới apply
     */
    @Transactional
    public void createNewApplicantNotification(Applicant applicant) {
        User employerUser = applicant.getJobPosting().getEmployer().getUser();
        String candidateName = applicant.getResume() != null ? applicant.getResume().getFullName() : "Ứng viên";
        String jobTitle = applicant.getJobPosting().getTitle();
        
        String title = "🔔 Có ứng viên mới ứng tuyển";
        String message = candidateName + " vừa ứng tuyển vào vị trí " + jobTitle + ". Vui lòng kiểm tra và xử lý.";
        
        Notification notification = Notification.builder()
                .user(employerUser)
                .applicant(applicant)
                .notificationType(NotificationType.NEW_APPLICANT)
                .title(title)
                .message(message)
                .status(ApplicationStatus.PENDING)
                .jobTitle(jobTitle)
                .companyName(null)
                .jobId(applicant.getJobPosting().getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notificationRepository.save(notification);
        log.info("Đã tạo thông báo NEW_APPLICANT cho employer {} về ứng viên mới {}", 
                 employerUser.getId(), applicant.getId());
    }
    
    /**
     * ✅ Tạo thông báo xác nhận ứng tuyển thành công cho candidate
     */
    @Transactional
    public void createApplySuccessNotification(Applicant applicant) {
        User candidateUser = applicant.getCandidate().getUser();
        String companyName = applicant.getJobPosting().getEmployer().getCompanyInformation().getCompanyName();
        String jobTitle = applicant.getJobPosting().getTitle();
        
        String title = "✅ Ứng tuyển thành công";
        String message = "Bạn đã ứng tuyển thành công vào vị trí " + jobTitle + " tại " + companyName + ". Đơn ứng tuyển đang được xét duyệt.";
        
        Notification notification = Notification.builder()
                .user(candidateUser)
                .applicant(applicant)
                .notificationType(NotificationType.APPLY_SUCCESS)
                .title(title)
                .message(message)
                .status(ApplicationStatus.PENDING)
                .jobTitle(jobTitle)
                .companyName(companyName)
                .jobId(applicant.getJobPosting().getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notificationRepository.save(notification);
        log.info("Đã tạo thông báo APPLY_SUCCESS cho candidate {} cho job {}", 
                 candidateUser.getId(), applicant.getId());
    }
    
    /**
     * ✅ Fix N+1: Lấy danh sách thông báo của user với phân trang và JOIN FETCH
     */
    public Map<String, Object> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findActiveByUserId(userId, pageable);
        
        List<NotificationResponseDto> dtos = notifications.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", dtos);
        response.put("currentPage", notifications.getNumber());
        response.put("totalPages", notifications.getTotalPages());
        response.put("totalItems", notifications.getTotalElements());
        response.put("hasNext", notifications.hasNext());
        response.put("hasPrevious", notifications.hasPrevious());
        
        return response;
    }
    
    /**
     * ✅ Pagination cho unread: Giới hạn số lượng
     */
    public Map<String, Object> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findUnreadByUserId(userId, pageable);
        
        List<NotificationResponseDto> dtos = notifications.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", dtos);
        response.put("unreadCount", unreadCount);
        response.put("currentPage", notifications.getNumber());
        response.put("totalPages", notifications.getTotalPages());
        response.put("hasNext", notifications.hasNext());
        
        return response;
    }
    
    /**
     * ✅ Đếm số thông báo chưa đọc
     */
    public Long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);
    }
    
    /**
     * ✅ Authorization: Đánh dấu thông báo đã đọc với kiểm tra ownership
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Notification not found or access denied"));
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("User {} đã đọc notification {}", userId, notificationId);
    }
    
    /**
     * ✅ Authorization: Đánh dấu tất cả thông báo đã đọc
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        // Query sẽ tự động check userId trong repository
        notificationRepository.markAllAsRead(userId);
        log.info("User {} đã đọc tất cả notifications", userId);
    }
    
    /**
     * ✅ Soft Delete: Xóa mềm thông báo
     */
    @Transactional
    public void softDeleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Notification not found or access denied"));
        
        notification.setDeletedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("User {} đã xóa notification {}", userId, notificationId);
    }
    
    // ✅ Helper methods
    
    private NotificationType mapStatusToNotificationType(ApplicationStatus status) {
        return switch (status) {
            case CV_PASSED -> NotificationType.STATUS_UPDATE_PASSED;
            case INTERVIEW -> NotificationType.STATUS_UPDATE_INTERVIEW;
            case HIRED -> NotificationType.STATUS_UPDATE_HIRED;
            case REJECTED -> NotificationType.STATUS_UPDATE_REJECTED;
            default -> NotificationType.APPLY_SUCCESS;
        };
    }
    
    // Helper methods
    
    private String getNotificationTitle(ApplicationStatus status, String companyName) {
        return switch (status) {
            case CV_PASSED -> "✅ CV của bạn đã được duyệt!";
            case INTERVIEW -> "📅 Mời phỏng vấn";
            case HIRED -> "🎉 Chúc mừng! Bạn đã được tuyển dụng";
            case REJECTED -> "Thông báo từ " + companyName;
            default -> "Cập nhật đơn ứng tuyển";
        };
    }
    
    private String getNotificationMessage(ApplicationStatus status, String jobTitle, String companyName, String note) {
        String baseMessage = switch (status) {
            case CV_PASSED -> companyName + " đã duyệt CV của bạn cho vị trí " + jobTitle + ".";
            case INTERVIEW -> companyName + " mời bạn tham gia phỏng vấn cho vị trí " + jobTitle + ".";
            case HIRED -> "Chúc mừng! Bạn đã được " + companyName + " tuyển dụng cho vị trí " + jobTitle + ".";
            case REJECTED -> companyName + " đã cập nhật trạng thái đơn ứng tuyển của bạn cho vị trí " + jobTitle + ".";
            default -> companyName + " đã cập nhật trạng thái đơn ứng tuyển của bạn.";
        };
        
        if (note != null && !note.isEmpty()) {
            baseMessage += " Ghi chú: " + note;
        }
        
        return baseMessage;
    }
    
    private NotificationResponseDto convertToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType() != null ? notification.getNotificationType().name() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus() != null ? notification.getStatus().name() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .applicantId(notification.getApplicant() != null ? notification.getApplicant().getId() : null)
                .jobTitle(notification.getJobTitle())
                .companyName(notification.getCompanyName())
                .jobId(notification.getJobId())
                .build();
    }
}
