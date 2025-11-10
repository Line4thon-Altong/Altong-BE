package com.altong.altong_backend.quiz.repository;


import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.quiz.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    boolean existsByQuizAndEmployee(Quiz quiz, Employee employee);
    Optional<QuizAttempt> findFirstByQuizAndEmployee(Quiz quiz, Employee employee);
}
