package com.example.WorkWite_Repo_BE.controlers;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.WorkWite_Repo_BE.dtos.Stats.OverviewStatsDto;
import com.example.WorkWite_Repo_BE.services.StatisticsService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    // Removed unused endpoint: GET /api/statistics/top-companies
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<OverviewStatsDto> getOverviewStats() {
        OverviewStatsDto stats = statisticsService.getOverviewStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData(@RequestParam int year) {
        Map<String, Object> result = new HashMap<>();
        result.put("userGrowth", statisticsService.getUserGrowth(year));
        result.put("jobPosting", statisticsService.getJobPostingGrowth(year));
        result.put("applicationStats", statisticsService.getApplicationStats());
        return ResponseEntity.ok(result);
    }
///  employer
    @GetMapping("/employer/{employerId}/overview")
    public ResponseEntity<Map<String, Object>> getEmployerOverviewStats(@PathVariable Long employerId) {
        Map<String, Object> stats = statisticsService.getEmployerOverviewStats(employerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/employer/{employerId}/chart-data")
    public ResponseEntity<Map<String, Object>> getEmployerChartData(
            @PathVariable Long employerId,
            @RequestParam int year) {
        Map<String, Object> result = new HashMap<>();
        result.put("jobPostingGrowth", statisticsService.getEmployerJobPostingGrowth(employerId, year));
        result.put("applicantStats", statisticsService.getEmployerApplicantStats(employerId, year));
        return ResponseEntity.ok(result);
    }
}