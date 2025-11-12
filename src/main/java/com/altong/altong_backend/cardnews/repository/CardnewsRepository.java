package com.altong.altong_backend.cardnews.repository;

import com.altong.altong_backend.cardnews.model.CardNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardnewsRepository extends JpaRepository<CardNews, Long> {
    void deleteByTrainingId(Long trainingId);
}
