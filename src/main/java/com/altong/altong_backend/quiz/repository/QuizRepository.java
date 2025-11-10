package com.altong.altong_backend.quiz.repository;

import com.altong.altong_backend.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT q FROM Quiz q WHERE q.training.id = :trainingId ORDER BY q.id ASC")
    List<Quiz> findByTraining_IdOrderByIdAsc(@Param("trainingId") Long trainingId);
}
