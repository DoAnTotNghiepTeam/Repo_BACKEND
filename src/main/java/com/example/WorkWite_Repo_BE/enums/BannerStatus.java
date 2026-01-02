package com.example.WorkWite_Repo_BE.enums;

public enum BannerStatus {
    PENDING,  // Chờ admin duyệt
    ACTIVE,  //// Đang hiển thị (đã duyệt và đến ngày)
    REJECTED,   // Admin từ chối
    EXPIRED,    // Đã hết hạn
    APPROVED    // Admin đã duyệt nhưng chưa đến ngày
} 