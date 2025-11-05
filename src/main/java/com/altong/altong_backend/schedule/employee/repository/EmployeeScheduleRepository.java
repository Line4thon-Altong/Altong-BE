package com.altong.altong_backend.schedule.employee.repository;

import com.altong.altong_backend.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmployeeScheduleRepository extends JpaRepository<Schedule, Long> {
    // 특정 직원의 특정 날짜 스케줄 조회 (출근/퇴근 처리용)
    Optional<Schedule> findByEmployee_IdAndWorkDate(Long employeeId, LocalDate workDate);
}
