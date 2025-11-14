package com.altong.altong_backend.schedule.owner.repository;

import com.altong.altong_backend.schedule.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OwnerScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByStore_IdAndEmployee_IdOrderByWorkDateDesc(Long storeId, Long employeeId);

    List<Schedule> findByStore_IdOrderByWorkDateAsc(Long storeId);

    List<Schedule> findByStore_IdAndWorkDate(Long storeId, LocalDate workDate);

    List<Schedule> findByStore_IdAndWorkDateBetweenOrderByWorkDateAsc(Long storeId, LocalDate startDate, LocalDate endDate);

}
