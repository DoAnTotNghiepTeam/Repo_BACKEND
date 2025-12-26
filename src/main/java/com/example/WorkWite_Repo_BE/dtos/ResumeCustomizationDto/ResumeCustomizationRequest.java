package com.example.WorkWite_Repo_BE.dtos.ResumeCustomizationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ResumeCustomizationRequest {
    private String font;
    private String colorScheme;
    private String customColor;
    private Integer spacing;
    private Integer fontSize;
    private String backgroundPattern;
}
