package com.altong.altong_backend.training.model;

import com.altong.altong_backend.global.util.JsonConverter;
import com.altong.altong_backend.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "training")
public class Training {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcedureStep {
        private String step;
        private List<String> details;
    }
}
