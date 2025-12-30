package com.example.WorkWite_Repo_BE.repositories;

import com.example.WorkWite_Repo_BE.entities.Banner;
import com.example.WorkWite_Repo_BE.enums.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
	List<Banner> findByBannerTypeAndStatus(String bannerType, BannerStatus status);

	List<Banner> findByStatus(BannerStatus status);
}