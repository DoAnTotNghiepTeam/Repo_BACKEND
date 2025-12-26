package com.example.WorkWite_Repo_BE.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resume_customizations")
public class ResumeCustomization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "font")
    private String font;

    @Column(name = "color_scheme")
    private String colorScheme;

    @Column(name = "custom_color")
    private String customColor;

    @Column(name = "spacing")
    private Integer spacing;

    @Column(name = "font_size")
    private Integer fontSize;

    @Column(name = "background_pattern")
    private String backgroundPattern;

}
