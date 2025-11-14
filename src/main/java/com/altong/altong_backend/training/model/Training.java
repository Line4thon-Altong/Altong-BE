package com.altong.altong_backend.training.model;

import com.altong.altong_backend.employee.model.EmployeeTraining;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.manual.model.Manual;
import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.cardnews.model.CardNews;
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    private Manual manual;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes;

    @OneToOne(mappedBy = "training", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private CardNews cardNews;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeTraining> employeeTrainings;

    public void setManual(Manual manual) {
        this.manual = manual;
        if (manual.getTraining() != this) {
            manual.setTraining(this);
        }
    }
}
