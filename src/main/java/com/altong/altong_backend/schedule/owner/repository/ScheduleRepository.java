package com.altong.altong_backend.schedule.owner.repository;

import com.altong.altong_backend.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 매장의 특정 직원 스케줄 조회
    List<Schedule> findByStore_IdAndEmployee_IdOrderByWorkDateDesc(Long storeId, Long employeeId);
    
    // 특정 매장의 전체 스케줄 조회
    List<Schedule> findByStore_IdOrderByWorkDateDesc(Long storeId);
    
    // 특정 매장의 특정 날짜 스케줄 조회
    List<Schedule> findByStore_IdAndWorkDate(Long storeId, LocalDate workDate);
}
