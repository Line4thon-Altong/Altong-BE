package com.altong.altong_backend.quiz.repository;

import com.altong.altong_backend.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTraining_Id(Long trainingId);
}
