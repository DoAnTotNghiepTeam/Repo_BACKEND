package com.example.WorkWite_Repo_BE.controlers;

import com.example.WorkWite_Repo_BE.dtos.Notification.NotificationResponseDto;
import com.example.WorkWite_Repo_BE.services.AuthService;
import com.example.WorkWite_Repo_BE.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final AuthService authService;
    
    /**
     * Lấy danh sách thông báo của user hiện tại với phân trang
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = authService.getCurrentUser().getId();
        Map<String, Object> response = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ✅ Lấy thông báo chưa đọc (có pagination)
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = authService.getCurrentUser().getId();
        Map<String, Object> response = notificationService.getUnreadNotifications(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        Long userId = authService.getCurrentUser().getId();
        Long count = notificationService.countUnreadNotifications(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đánh dấu một thông báo là đã đọc
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId) {
        Long userId = authService.getCurrentUser().getId();
        notificationService.markAsRead(notificationId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã đánh dấu thông báo là đã đọc");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Long userId = authService.getCurrentUser().getId();
        notificationService.markAllAsRead(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã đánh dấu tất cả thông báo là đã đọc");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * ✅ Soft Delete: Xóa mềm một thông báo
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long notificationId) {
        Long userId = authService.getCurrentUser().getId();
        notificationService.softDeleteNotification(notificationId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã xóa thông báo thành công");
        
        return ResponseEntity.ok(response);
    }
}
