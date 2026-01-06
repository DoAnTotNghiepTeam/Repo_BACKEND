package com.example.WorkWite_Repo_BE.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler để tự động kích hoạt và hết hạn banners theo thời gian xem thử banner có còn hdong không
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BannerScheduler {
    
    private final BannerService bannerService;
    
    /**
     * Chạy mỗi giờ (0 phút của mỗi giờ)
     * Kích hoạt các banner PENDING khi đến ngày bắt đầu
     */
    @Scheduled(cron = "0 0 * * * *")
    public void activatePendingBanners() {
        log.info("Running scheduled task: activateBannersIfNeeded");
        try {
            bannerService.activateBannersIfNeeded();
            log.info("Successfully activated pending banners");
        } catch (Exception e) {
            log.error("Error activating banners: " + e.getMessage(), e);
        }
    }
    
    /**
     * Chạy mỗi ngày lúc 00:05 (5 phút sau nửa đêm)
     * Chuyển các banner ACTIVE sang EXPIRED khi hết hạn
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void expireActiveBanners() {
        log.info("Running scheduled task: expireBannersIfNeeded");
        try {
            bannerService.expireBannersIfNeeded();
            log.info("Successfully expired banners");
        } catch (Exception e) {
            log.error("Error expiring banners: " + e.getMessage(), e);
        }
    }
}

// Giải thích Cron Expression:

// "0 0 * * * *" = Giây 0, Phút 0, Mọi giờ, Mọi ngày, Mọi tháng, Mọi năm

// Chạy mỗi giờ: 01:00:00, 02:00:00, 03:00:00...
// "0 5 0 * * *" = Giây 0, Phút 5, Giờ 0 (nửa đêm), Mọi ngày, Mọi tháng, Mọi năm

// Chạy mỗi ngày lúc 00:05:00