package com.example.WorkWite_Repo_BE.repositories;

import com.example.WorkWite_Repo_BE.entities.Banner;
import com.example.WorkWite_Repo_BE.enums.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
	    // Tìm banner theo loại VÀ trạng thái
    // VD: findByBannerTypeAndStatus("Vip", ACTIVE) 
    // → Trả về tất cả banner Vip đang ACTIVE
	List<Banner> findByBannerTypeAndStatus(String bannerType, BannerStatus status);

	    // Tìm banner theo trạng thái
    // VD: findByStatus(PENDING) → Tất cả banner đang chờ duyệt
	List<Banner> findByStatus(BannerStatus status);


	    // Method mới - tìm banner theo type và status  ( để ktra xem trong khoảng thời gian đó đã có ai thuebnner này chưa)
		    // Tìm banner đang hoạt động HOẶC chờ duyệt theo loại
    // VD: Kiểm tra slot "Vip" có ai đang thuê không
    @Query("SELECT b FROM Banner b WHERE b.bannerType = :type " +
           "AND (b.status = 'ACTIVE' OR b.status = 'PENDING' OR b.status = 'APPROVED')")
    List<Banner> findActiveOrPendingByType(@Param("type") String type);


	  // Tìm banner theo user
    // VD: Lấy tất cả banner của user ID = 5
	List<Banner> findByUserId(Long userId);


	    // Tìm banner PENDING có ngày bắt đầu <= hiện tại
    // VD: Hôm nay 10/1, tìm banner PENDING có startDate <= 10/1
    // → Những banner này cần chuyển sang ACTIVE
	    @Query("SELECT b FROM Banner b WHERE b.status = :status AND b.startDate <= :now")
    List<Banner> findByStatusAndStartDateBefore(
        @Param("status") BannerStatus status, 
        @Param("now") LocalDateTime now
    );
    

	    // Tìm banner ACTIVE đã hết hạn
    // VD: Hôm nay 20/1, tìm banner ACTIVE có endDate < 20/1
    // → Những banner này cần chuyển sang EXPIRED
    @Query("SELECT b FROM Banner b WHERE b.status = :status AND b.endDate < :now")
    List<Banner> findByStatusAndEndDateBefore(
        @Param("status") BannerStatus status, 
        @Param("now") LocalDateTime now
    );
	// 
}