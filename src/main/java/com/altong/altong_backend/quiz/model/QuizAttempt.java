package com.altong.altong_backend.quiz.model;

import com.altong.altong_backend.employee.model.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quiz_attempt")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private Boolean isCorrect;

    @Column(nullable = false)
    private String selectedAnswer;

    private LocalDateTime submittedAt;

    public static QuizAttempt of(Quiz quiz, Employee employee, boolean isCorrect, String selectedAnswer) {
        return QuizAttempt.builder()
                .quiz(quiz)
                .employee(employee)
                .isCorrect(isCorrect)
                .selectedAnswer(selectedAnswer)
                .submittedAt(LocalDateTime.now())
                .build();
    }
}
