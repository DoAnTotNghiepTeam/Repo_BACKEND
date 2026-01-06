package com.example.WorkWite_Repo_BE.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.WorkWite_Repo_BE.enums.BannerStatus;

@Entity
@Table(name = "banners")
@Getter
@Setter
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thông tin công ty
    private String companyName;
    private String companyEmail;
    private String companyPhone;

    // Thông tin banner
    private String bannerImage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long amount; // Số tiền đã trừ khi thuê banner
    private String bannerType; // Vip, Featured, Standard

    @Enumerated(EnumType.STRING)
    private BannerStatus status; // PENDING, ACTIVE, REJECTED, EXPIRED

    private LocalDateTime createdAt;    // ngfay tạo
    private LocalDateTime updatedAt;    // ngày cập 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;     // Thuộc về user nào (nhiều banner → 1 user)
}
