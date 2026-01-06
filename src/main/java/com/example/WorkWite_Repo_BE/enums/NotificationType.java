package com.example.WorkWite_Repo_BE.enums;

public enum NotificationType {
    // Candidate notifications
    APPLY_SUCCESS,           // Ứng tuyển thành công
    STATUS_UPDATE_PASSED,    // CV được duyệt
    STATUS_UPDATE_INTERVIEW, // Mời phỏng vấn
    STATUS_UPDATE_HIRED,     // Được tuyển dụng
    STATUS_UPDATE_REJECTED,  // Bị từ chối
    
    // Employer notifications
    NEW_APPLICANT           // Có ứng viên mới
}
