    package com.example.WorkWite_Repo_BE.services;

    import com.example.WorkWite_Repo_BE.dtos.CompanyInformation.CompanyInformationReponseDto;
    import com.example.WorkWite_Repo_BE.dtos.JobScore.JobScoreDto;
    import com.example.WorkWite_Repo_BE.dtos.JobScore.RecommendationResponseDto;
    import com.example.WorkWite_Repo_BE.entities.JobPosting;
    import com.example.WorkWite_Repo_BE.entities.Resume;
    import com.example.WorkWite_Repo_BE.repositories.ExperienceJpaRepository;
    import com.example.WorkWite_Repo_BE.repositories.JobPostingRepository;
    import com.example.WorkWite_Repo_BE.repositories.ResumeJpaRepository;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.time.temporal.ChronoUnit;
    import java.util.*;
    import java.util.stream.Collectors;

    //@Service
    //@RequiredArgsConstructor
    //    public class RecommendationService {
    //    private final ResumeJpaRepository resumeRepository;
    //    private final JobPostingRepository jobPostingRepository;
    //    private final ExperienceJpaRepository experienceRepository;
    //    private final CompanyInformationService companyInformationService;
    //
    //
    //    private double estimateExperienceYears(Long resumeId) {
    //        return experienceRepository.findByResumeId(resumeId).stream()
    //                .mapToDouble(e -> {
    //                    if (e.getStartYear() != null && e.getEndYear() != null) {
    //                        return ChronoUnit.YEARS.between(e.getStartYear(), e.getEndYear());
    //                    }
    //                    return 0.0;
    //                })
    //                .sum();
    //    }
    //
    //    private double round(double value) {
    //        return Math.round(value * 100.0) / 100.0; // làm tròn 2 chữ số thập phân
    //    }
    //    private String normalizeDegree(String input) {
    //        if (input == null) return "";
    //
    //        String text = input.toLowerCase().trim();
    //
    //        // Tiến sĩ / PhD
    //        if (text.contains("tiến sĩ") || text.contains("phd") || text.contains("doctor of philosophy") || text.contains("dr.")) {
    //            return "phd";
    //        }
    //
    //        // Thạc sĩ / Master
    //        if (text.contains("thạc sĩ") || text.contains("master") || text.contains("msc") || text.contains("ma") || text.contains("m.eng") || text.contains("mtech")) {
    //            return "master";
    //        }
    //
    //        // Đại học / Cử nhân / Bachelor / Kỹ sư
    //        if (text.contains("đại học") || text.contains("cử nhân")
    //                || text.contains("bachelor") || text.contains("ba")
    //                || text.contains("bs") || text.contains("b.sc")
    //                || text.contains("b.eng") || text.contains("kỹ sư")
    //                || text.contains("engineer")) {
    //            return "bachelor";
    //        }
    //
    //        // Cao đẳng / College / Diploma
    //        if (text.contains("cao đẳng") || text.contains("college")
    //                || text.contains("diploma") || text.contains("associate degree")) {
    //            return "college";
    //        }
    //
    //        // Trung cấp / Associate
    //        if (text.contains("trung cấp") || text.contains("associate") || text.contains("vocational")) {
    //            return "associate";
    //        }
    //
    //        // Mặc định: không xác định
    //        return "";
    //    }
    //
    //    // weights
    //    private final double WEIGHT_SKILL = 0.60;
    //    private final double WEIGHT_EXPERIENCE = 0.25;
    //    private final double WEIGHT_DEGREE = 0.15;
    //
    //    @Transactional
    //    public RecommendationResponseDto recommendForCandidate(Long candidateId, int limit, double minScore) {
    //        Resume resume = resumeRepository.findTopByCandidateIdOrderByUpdatedAtDesc(candidateId)
    //                .orElseThrow(() -> new NoSuchElementException("Resume not found for candidate " + candidateId));
    //
    //        // Lấy skill từ CV
    //        List<String> candidateSkills = resume.getSkillsResumes()
    //                .stream()
    //                .map(String::toLowerCase)
    //                .map(String::trim)
    //                .toList();
    //
    //        // In ra kỹ năng từ hồ sơ
    //        System.out.println("===== Candidate Resume Info =====");
    //        System.out.println("CandidateId: " + candidateId);
    //        System.out.println("Skills: " + candidateSkills);
    //
    //        double candidateExpYears = estimateExperienceYears(resume.getId());
    //        System.out.println("Total Experience (years): " + candidateExpYears);
    //
    //        if (resume.getEducations() != null) {
    //            System.out.println("Educations: ");
    //            resume.getEducations().forEach(e -> {
    //                System.out.println(" - Degree: " + e.getDegree() + ", Major: " + e.getMajor() + ", School: " + e.getSchoolName());
    //            });
    //        }
    //
    //        List<JobScoreDto> results = new ArrayList<>();
    //
    //        for (JobPosting job : jobPostingRepository.findAll()) {
    //            // Lấy skill từ Job
    //            List<String> jobSkills = job.getRequiredSkills()
    //                    .stream()
    //                    .map(String::toLowerCase)
    //                    .map(String::trim)
    //                    .toList();
    //
    //            System.out.println("\n===== Job Posting Info =====");
    //            System.out.println("JobId: " + job.getId() + " | Title: " + job.getTitle());
    //            System.out.println("Required Skills: " + jobSkills);
    //            System.out.println("Min Experience (years): " + job.getMinExperience());
    //            System.out.println("Required Degree: " + job.getRequiredDegree());
    //
    //            int totalReq = jobSkills.size();
    //            int matched = (int) jobSkills.stream().filter(candidateSkills::contains).count();
    //            double skillMatchPercent = totalReq == 0 ? 0.0 : (matched * 100.0 / totalReq);
    //            double skillScore = skillMatchPercent / 100.0;
    //
    //            // kinh nghiệm
    //            double expScore = 0.0;
    //            if (job.getMinExperience() == null || job.getMinExperience() <= 0) {
    //                expScore = 1.0;
    //            } else {
    //                expScore = Math.min(1.0, candidateExpYears / job.getMinExperience());
    //            }
    //
    //            // học vấn
    //            double degreeScore = 0.0;
    //            if (job.getRequiredDegree() == null || job.getRequiredDegree().isBlank()) {
    //                degreeScore = 1.0; // không yêu cầu => auto match
    //            } else if (resume.getEducations() != null) {
    //                String requiredNorm = normalizeDegree(job.getRequiredDegree());
    //
    //                boolean matchedEducation = resume.getEducations().stream()
    //                        .map(e -> normalizeDegree(e.getDegree()))
    //                        .anyMatch(degree -> !degree.isEmpty() && degree.equals(requiredNorm));
    //
    //                if (matchedEducation) {
    //                    degreeScore = 1.0;
    //                }
    //            }
    //
    //            double finalScore = (
    //                    WEIGHT_SKILL * skillScore +
    //                            WEIGHT_EXPERIENCE * expScore +
    //                            WEIGHT_DEGREE * degreeScore
    //            ) * 100.0;
    //
    //            CompanyInformationReponseDto companyInfo =
    //                    companyInformationService.getCompanyByEmployerId(job.getEmployer().getId());
    //
    //
    //            System.out.println("Matched Skills: " + matched + "/" + totalReq);
    //            System.out.println("Skill Match %: " + round(skillMatchPercent));
    //            System.out.println("Exp Score: " + round(expScore * 100) + "%");
    //            System.out.println("Degree Score: " + degreeScore);
    //            System.out.println("Final Score: " + round(finalScore));
    //
    //            if (finalScore >= minScore && matched > 0) {
    //                results.add(new JobScoreDto(
    //                        job.getId(),
    //                        job.getTitle(),
    //                        job.getSalaryRange(),
    //                        job.getLocation(),
    //                        companyInfo.getLogoUrl(),
    //                        job.getDescription(),
    //                        companyInfo.getCompanyName(),
    //                        resume.getEducations().get(0).getMajor(),
    //                        round(finalScore),
    //                        matched,
    //                        totalReq,
    //                        expScore,
    //                        degreeScore,
    //                        round(skillMatchPercent)
    //                ));
    //            }
    //        }
    //
    //        results.sort(Comparator.comparingDouble(JobScoreDto::getScore).reversed());
    //        if (limit > 0 && results.size() > limit) {
    //            results = results.subList(0, limit);
    //        }
    //        return new RecommendationResponseDto(candidateId, results);
    //    }
    //}

    @Service
    @RequiredArgsConstructor
    public class RecommendationService {

        private final ResumeJpaRepository resumeRepository;
        private final JobPostingRepository jobPostingRepository;
        private final ExperienceJpaRepository experienceRepository;
        private final CompanyInformationService companyInformationService;

        // ==============================
        // 1. TÍNH NĂM KINH NGHIỆM
        // ==============================
        private double estimateExperienceYears(Long resumeId) {
            return experienceRepository.findByResumeId(resumeId).stream()
                    .mapToDouble(e -> {
                        if (e.getStartYear() != null && e.getEndYear() != null) {
                            return ChronoUnit.YEARS.between(e.getStartYear(), e.getEndYear());
                        }
                        return 0.0;
                    })
                    .sum();
        }

        private double round(double value) {
            return Math.round(value * 100.0) / 100.0;
        }

        // ==============================
        // 2. CHUẨN HÓA HỌC VỊ
        // ==============================
        private String normalizeDegree(String input) {
            if (input == null) return "";
            String text = input.toLowerCase().trim();

            if (text.contains("phd") || text.contains("tiến sĩ") || text.contains("doctor")) return "phd";
            if (text.contains("thạc sĩ") || text.contains("master") || text.contains("msc")) return "master";
            if (text.contains("đại học") || text.contains("bachelor") || text.contains("cử nhân")) return "bachelor";
            if (text.contains("cao đẳng") || text.contains("college") || text.contains("diploma")) return "college";
            if (text.contains("trung cấp") || text.contains("associate")) return "associate";

            return "";
        }

        // ==============================
            // 3. AI - TF-IDF + COSINE SIMILARITY
        // ==============================

        private Map<String, Double> tfidfVector(String text, List<String> allDocs) {

            Map<String, Double> vector = new HashMap<>();
            if (text == null) return vector;

            // tách từ thô
            String[] words = text.toLowerCase().replaceAll("[^a-z0-9]", " ").split("\\s+");

            Map<String, Long> termFreq = Arrays.stream(words)
                    .filter(w -> w.length() > 2)
                    .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

            for (String term : termFreq.keySet()) {
                long docCount = allDocs.stream()
                        .filter(d -> d.toLowerCase().contains(term))
                        .count();

                if (docCount == 0) continue;

                double tf = termFreq.get(term);
                double idf = Math.log10(allDocs.size() / (1.0 + docCount));

                vector.put(term, tf * idf);
            }

            return vector;
        }

        private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {

            Set<String> terms = new HashSet<>();
            terms.addAll(v1.keySet());
            terms.addAll(v2.keySet());

            double dot = 0, m1 = 0, m2 = 0;

            for (String key : terms) {
                double x = v1.getOrDefault(key, 0.0);
                double y = v2.getOrDefault(key, 0.0);

                dot += x * y;
                m1 += x * x;
                m2 += y * y;
            }

            return m1 == 0 || m2 == 0 ? 0 : dot / (Math.sqrt(m1) * Math.sqrt(m2));
        }

        // ==============================
        // 4. WEIGHTS
        // ==============================
        private final double WEIGHT_SKILL = 0.50;        // rule-based
        private final double WEIGHT_EXPERIENCE = 0.20;   // rule-based
        private final double WEIGHT_DEGREE = 0.10;       // rule-based
        private final double WEIGHT_AI = 0.20;           // AI TF-IDF layer

        // ==============================
        // 5. MAIN RECOMMEND METHOD
        // ==============================
        @Transactional
        public RecommendationResponseDto recommendForCandidate(Long candidateId, int limit, double minScore) {

            Resume resume = resumeRepository.findTopByCandidateIdOrderByUpdatedAtDesc(candidateId)
                    .orElseThrow(() -> new NoSuchElementException("Resume not found"));

            // skill từ resume
            List<String> candidateSkills = resume.getSkillsResumes().stream()
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .toList();

            double candidateExpYears = estimateExperienceYears(resume.getId());

            // ==============================
            // Làm văn bản CV để AI xử lý
            // ==============================
            String resumeText =
                    resume.getSummary() + " " +
                            resume.getJobTitle() + " " +
                            candidateSkills.toString();

            List<String> corpus = jobPostingRepository.findAll().stream()
                    .map(j -> j.getDescription() + " " + j.getTitle())
                    .toList();

            Map<String, Double> resumeVector = tfidfVector(resumeText, corpus);

            // ==============================
            // 6. LOOP JOBS
            // ==============================
            List<JobScoreDto> results = new ArrayList<>();

            for (JobPosting job : jobPostingRepository.findAll()) {

                List<String> jobSkills = job.getRequiredSkills()
                        .stream().map(String::toLowerCase).toList();

                // === (1) Skill matching ===
                int totalReq = jobSkills.size();
                int matched = (int) jobSkills.stream()
                        .filter(candidateSkills::contains)
                        .count();

                double skillMatchPercent =
                        totalReq == 0 ? 0 : (matched * 100.0 / totalReq);

                double skillScore = skillMatchPercent / 100.0;

                // === (2) Experience ===
                double expScore = 0;
                if (job.getMinExperience() == null || job.getMinExperience() <= 0)
                    expScore = 1;
                else
                    expScore = Math.min(1.0, candidateExpYears / job.getMinExperience());

                // === (3) Degree ===
                double degreeScore = 0;
                if (job.getRequiredDegree() == null)
                    degreeScore = 1;
                else if (resume.getEducations() != null) {
                    String requiredNorm = normalizeDegree(job.getRequiredDegree());
                    boolean ok = resume.getEducations().stream()
                            .map(e -> normalizeDegree(e.getDegree()))
                            .anyMatch(d -> d.equals(requiredNorm));
                    degreeScore = ok ? 1 : 0;
                }

                // === (4) AI Cosine TF-IDF ===
                String jobText = job.getDescription() + " " + job.getTitle();
                Map<String, Double> jobVector = tfidfVector(jobText, corpus);
                double aiSimilarity = cosineSimilarity(resumeVector, jobVector); // 0 → 1

                // === FINAL SCORE (0 → 100) ===
                double finalScore =
                        (skillScore * WEIGHT_SKILL +
                                expScore * WEIGHT_EXPERIENCE +
                                degreeScore * WEIGHT_DEGREE +
                                aiSimilarity * WEIGHT_AI)
                                * 100;

                if (finalScore >= minScore) {

                    CompanyInformationReponseDto companyInfo =
                            companyInformationService.getCompanyByEmployerId(job.getEmployer().getId());

                    results.add(new JobScoreDto(
                            job.getId(),
                            job.getTitle(),
                            job.getSalaryRange(),
                            job.getLocation(),
                            companyInfo.getLogoUrl(),
                            job.getDescription(),
                            companyInfo.getCompanyName(),
                            resume.getEducations().isEmpty() ? "" :
                                    resume.getEducations().get(0).getMajor(),
                            round(finalScore),
                            matched,
                            totalReq,
                            expScore,
                            degreeScore,
                            round(skillMatchPercent)
                    ));
                }
            }

            results.sort(Comparator.comparingDouble(JobScoreDto::getScore).reversed());

            if (limit > 0 && results.size() > limit)
                results = results.subList(0, limit);

            return new RecommendationResponseDto(candidateId, results);
        }
    }
