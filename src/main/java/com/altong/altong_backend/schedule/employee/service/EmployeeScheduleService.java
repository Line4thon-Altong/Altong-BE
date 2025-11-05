package com.altong.altong_backend.schedule.employee.service;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.schedule.employee.dto.response.CheckInResponse;
import com.altong.altong_backend.schedule.employee.repository.EmployeeScheduleRepository;
import com.altong.altong_backend.schedule.entity.Schedule;
import com.altong.altong_backend.schedule.entity.WorkStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class EmployeeScheduleService {

    private final EmployeeScheduleRepository employeeScheduleRepository;

    @Transactional
    public CheckInResponse checkIn(Long employeeId) {
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        LocalTime now = LocalTime.now(); // 지금 시간 가져오기

        // 오늘 스케줄 조회
        Schedule schedule = employeeScheduleRepository.findByEmployee_IdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND_TODAY));

        // 이미 출근 처리되었는지 확인
        if (schedule.getStartTime() != null) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }

        // 출근 처리
        Schedule updated = Schedule.builder()
                .id(schedule.getId())
                .workDate(schedule.getWorkDate())
                .startTime(now)
                .endTime(schedule.getEndTime())
                .workStatus(WorkStatus.WORKING)
                .employee(schedule.getEmployee())
                .store(schedule.getStore())
                .build();

        Schedule saved = employeeScheduleRepository.save(updated);
        return CheckInResponse.from(saved);
    }
}
