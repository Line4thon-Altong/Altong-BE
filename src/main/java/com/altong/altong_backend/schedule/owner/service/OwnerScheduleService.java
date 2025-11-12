package com.altong.altong_backend.schedule.owner.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.schedule.entity.Schedule;
import com.altong.altong_backend.schedule.entity.WorkStatus;
import com.altong.altong_backend.schedule.owner.dto.request.ScheduleCreateRequest;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleListResponse;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleResponse;
import com.altong.altong_backend.schedule.owner.repository.OwnerScheduleRepository;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Builder
@AllArgsConstructor
public class OwnerScheduleService {
    private final OwnerScheduleRepository ownerScheduleRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public List<ScheduleResponse> createSchedule(Long storeId,Long employeeId,ScheduleCreateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 해당 직원이 해당 매장 소속인지 확인
        if (employee.getStore() == null || !employee.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.EMPLOYEE_NOT_BELONG_TO_STORE);
        }

        List<Schedule> schedules = request.workDates().stream()
                .map(workDate -> Schedule.builder()
                        .store(store)
                        .employee(employee)
                        .workDate(workDate)
                        .startTime(null) // 알바생이 입력할 필드
                        .endTime(null)
                        .workStatus(WorkStatus.SCHEDULED)
                        .build())
                .toList();

        List<Schedule> savedSchedules = ownerScheduleRepository.saveAll(schedules);

        return savedSchedules.stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleListResponse getEmployeeSchedules(Long storeId, Long employeeId) {

        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 해당 직원이 해당 매장 소속인지까지 확인
        if (employee.getStore() == null || !employee.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.EMPLOYEE_NOT_BELONG_TO_STORE);
        }

        // 특정 매장의 특정 직원 스케줄 조회
        List<Schedule> schedules = ownerScheduleRepository.findByStore_IdAndEmployee_IdOrderByWorkDateDesc(storeId, employeeId);

        List<ScheduleResponse> scheduleResponses = schedules.stream()
                .map(ScheduleResponse::from)
                .toList();

        return new ScheduleListResponse(scheduleResponses);
    }

    @Transactional(readOnly = true)
    public ScheduleListResponse getStoreSchedules(Long ownerId,Long storeId, LocalDate workDate,Integer year,Integer month) {


        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        List<Schedule> schedules;

        // month만 있을 때 -> 잘못된 요청
        if (year == null && month != null) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // year만 있을 때 -> 해당 연도 전체 조회
        if (year != null && month == null) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            schedules = ownerScheduleRepository.findByStore_IdAndWorkDateBetweenOrderByWorkDateAsc(
                    storeId, startDate, endDate
            );
        }
        // year, month 둘 다 있을 때 -> 월별 조회
        else if (year != null && month != null) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            schedules = ownerScheduleRepository.findByStore_IdAndWorkDateBetweenOrderByWorkDateAsc(
                    storeId, startDate, endDate
            );
        }
        // 특정 날짜 조회
        else if (workDate != null) {
            schedules = ownerScheduleRepository.findByStore_IdAndWorkDate(storeId, workDate);
        }
        // 전체 조회
        else {
            schedules = ownerScheduleRepository.findByStore_IdOrderByWorkDateAsc(storeId);
        }

        List<ScheduleResponse> scheduleResponses = schedules.stream()
                .map(ScheduleResponse::from)
                .toList();

        return new ScheduleListResponse(scheduleResponses);
    }

    @Transactional
    public void deleteSchedule(Long storeId, Long scheduleId) {
        // 스케줄 존재 여부
        Schedule schedule = ownerScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 해당 스케줄이 해당 매장의 스케줄인지 확인
        if (schedule.getStore() == null || !schedule.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_BELONG_TO_STORE);
        }

        ownerScheduleRepository.delete(schedule);
    }
}
