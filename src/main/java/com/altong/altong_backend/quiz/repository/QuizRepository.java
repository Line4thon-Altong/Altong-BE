package com.altong.altong_backend.quiz.repository;

import com.altong.altong_backend.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
