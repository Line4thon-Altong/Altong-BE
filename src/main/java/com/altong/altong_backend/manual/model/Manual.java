package com.altong.altong_backend.manual.model;

import com.altong.altong_backend.global.util.JsonConverter;
import com.altong.altong_backend.training.model.Training;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "manual")
public class Manual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String goal;

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private List<ProcedureStep> procedure;

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private List<String> precaution;

    @Column(name = "ai_raw_response", columnDefinition = "TEXT", nullable = false)
    private String aiRawResponse;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcedureStep {
        private String step;
        private List<String> details;
    }
}
