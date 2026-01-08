package com.example.WorkWite_Repo_BE.services;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.example.WorkWite_Repo_BE.enums.ApplicationStatus;
import com.example.WorkWite_Repo_BE.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.WorkWite_Repo_BE.dtos.ChartData.ChartDataPoint;
import com.example.WorkWite_Repo_BE.dtos.Stats.OverviewStatsDto;

@Service
public class StatisticsService {
    /**
     * Trả về danh sách top công ty được ứng viên apply nhiều nhất
     */
    // Removed unused: getTopCompaniesByApplications()
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private EmployersJpaRepository employerRepo;
    @Autowired
    private CandidateJpaRepository candidateRepo;
    @Autowired
    private JobPostingRepository jobRepo;
    @Autowired
    private ApplicantRepository applicantRepo;

    public OverviewStatsDto getOverviewStats() {
        OverviewStatsDto dto = new OverviewStatsDto();
        dto.setTotalUsers(userRepo.count());
        dto.setTotalEmployers(employerRepo.countByStatus("APPROVED"));
        dto.setTotalCandidates(candidateRepo.count());
        dto.setTotalJobPostings(jobRepo.count());
        dto.setTotalApplications(applicantRepo.count());
        return dto;
    }

    public List<ChartDataPoint> getUserGrowth(int year) {
        List<Object[]> results = userRepo.countUserByMonth(year);
        List<ChartDataPoint> data = new ArrayList<>();
        for (Object[] row : results) {
            int monthNum = (int) row[0];
            long value = (long) row[1];
            String month = Month.of(monthNum).name().substring(0, 3); // "JAN", "FEB", ...
            data.add(new ChartDataPoint(month, value));
        }
        return data;
    }

    public List<ChartDataPoint> getJobPostingGrowth(int year) {
        List<Object[]> results = jobRepo.countJobPostingByMonth(year);
        List<ChartDataPoint> data = new ArrayList<>();
        for (Object[] row : results) {
            int monthNum = (int) row[0];
            long value = (long) row[1];
            String month = Month.of(monthNum).name().substring(0, 3);
            data.add(new ChartDataPoint(month, value));
        }
        return data;
    }

    /**
     * Thống kê ứng tuyển: tổng số lượt ứng tuyển và tỉ lệ ứng viên ứng tuyển/số tin đăng
     */
    public Map<String, Object> getApplicationStats() {
        long totalApplications = applicantRepo.count();
        long totalJobPostings = jobRepo.count();
        double applyRate = totalJobPostings > 0 ? (totalApplications * 1.0 / totalJobPostings) : 0;
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", totalApplications);
        stats.put("applyRate", applyRate);
        return stats;
    }

    /**
     * Thống kê tổng quan của employer
     */
    public Map<String, Object> getEmployerOverviewStats(Long employerId) {
        long totalJobPostings = jobRepo.countByEmployerId(employerId);
        long totalApplicants = applicantRepo.countByEmployerId(employerId);
        long acceptedApplicants = applicantRepo.countByEmployerIdAndStatus(employerId, ApplicationStatus.HIRED);
        long rejectedApplicants = applicantRepo.countByEmployerIdAndStatus(employerId, ApplicationStatus.REJECTED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobPostings", totalJobPostings);
        stats.put("totalApplicants", totalApplicants);
        stats.put("acceptedApplicants", acceptedApplicants);
        stats.put("rejectedApplicants", rejectedApplicants);
        return stats;
    }

    /**
     * Thống kê tăng trưởng job posting theo tháng của employer
     */
    public List<ChartDataPoint> getEmployerJobPostingGrowth(Long employerId, int year) {
        List<Object[]> results = jobRepo.countJobPostingByEmployerAndMonth(employerId, year);
        List<ChartDataPoint> data = new ArrayList<>();
        for (Object[] row : results) {
            int monthNum = (int) row[0];
            long value = (long) row[1];
            String month = Month.of(monthNum).name().substring(0, 3);
            data.add(new ChartDataPoint(month, value));
        }
        return data;
    }

    /**
     * Thống kê số lượng ứng viên apply theo tháng của employer
     */
    public List<ChartDataPoint> getEmployerApplicantStats(Long employerId, int year) {
        List<Object[]> results = applicantRepo.countApplicantsByEmployerAndMonth(employerId, year);
        List<ChartDataPoint> data = new ArrayList<>();
        for (Object[] row : results) {
            int monthNum = (int) row[0];
            long value = (long) row[1];
            String month = Month.of(monthNum).name().substring(0, 3);
            data.add(new ChartDataPoint(month, value));
        }
        return data;
    }

}