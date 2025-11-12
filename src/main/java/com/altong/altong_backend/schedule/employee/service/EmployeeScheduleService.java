package com.altong.altong_backend.schedule.employee.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.schedule.employee.dto.response.CheckInResponse;
import com.altong.altong_backend.schedule.employee.dto.response.CheckOutResponse;
import com.altong.altong_backend.schedule.employee.repository.EmployeeScheduleRepository;
import com.altong.altong_backend.schedule.entity.Schedule;
import com.altong.altong_backend.schedule.entity.WorkStatus;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleListResponse;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeScheduleService {

    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final EmployeeRepository employeeRepository;

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

    @Transactional
    public CheckOutResponse checkOut(Long employeeId) {
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        LocalTime now = LocalTime.now(); // 지금 시간 가져오기

        Schedule schedule = employeeScheduleRepository.findByEmployee_IdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND_TODAY));

        if ((schedule.getEndTime() != null)) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_OUT);
        }

        // 퇴근 처리
        Schedule updated = Schedule.builder()
                .id(schedule.getId())
                .workDate(schedule.getWorkDate())
                .startTime(schedule.getStartTime())
                .endTime(now)
                .workStatus(WorkStatus.COMPLETED)
                .employee(schedule.getEmployee())
                .store(schedule.getStore())
                .build();

        Schedule saved = employeeScheduleRepository.save(updated);

        return CheckOutResponse.from(saved);

    }

    @Transactional(readOnly = true)
    public ScheduleListResponse getEmployeeSchedules(Long employeeId, LocalDate workDate,Integer year, Integer month) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        List<Schedule> schedules;

        // year, month 있으면 월별 조회
        if (year != null && month != null) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            schedules = employeeScheduleRepository.findByEmployee_IdAndWorkDateBetweenOrderByWorkDateAsc(
                    employeeId, startDate, endDate
            );
        }
        // workDate 있으면 특정 날짜 조회
        else if (workDate != null) {
            schedules = employeeScheduleRepository.findByEmployee_IdAndWorkDate(employeeId, workDate)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        // 전체 조회
        else {
            schedules = employeeScheduleRepository.findAllByEmployee_IdOrderByWorkDateDesc(employeeId);
        }

        List<ScheduleResponse> scheduleResponses = schedules.stream()
                .map(ScheduleResponse::from)
                .toList();

        return new ScheduleListResponse(scheduleResponses);
    }

}

