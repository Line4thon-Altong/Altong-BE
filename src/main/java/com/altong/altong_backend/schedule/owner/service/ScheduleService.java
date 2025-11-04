package com.altong.altong_backend.schedule.owner.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.schedule.owner.dto.request.ScheduleCreateRequest;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleListResponse;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleResponse;
import com.altong.altong_backend.schedule.owner.repository.ScheduleRepository;
import com.altong.altong_backend.schedule.entity.Schedule;
import com.altong.altong_backend.schedule.entity.WorkStatus;
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
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ScheduleResponse createSchedule(Long storeId,Long employeeId,ScheduleCreateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 해당 직원이 해당 매장 소속인지 확인
        if (employee.getStore() == null || !employee.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.EMPLOYEE_NOT_BELONG_TO_STORE);
        }

        Schedule schedule = Schedule.builder()
                .store(store)
                .employee(employee)
                .workDate(request.workDate())
                .startTime(null) // 알바생 입력 필드
                .endTime(null)   // 알바생 입력 필드
                .workStatus(WorkStatus.SCHEDULED)
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        return ScheduleResponse.from(saved);
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
        List<Schedule> schedules = scheduleRepository.findByStore_IdAndEmployee_IdOrderByWorkDateDesc(storeId, employeeId);

        List<ScheduleResponse> scheduleResponses = schedules.stream()
                .map(ScheduleResponse::from)
                .toList();

        return new ScheduleListResponse(scheduleResponses);
    }

    @Transactional(readOnly = true)
    public ScheduleListResponse getStoreSchedules(Long storeId, LocalDate workDate) {

        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        List<Schedule> schedules;
        
        // 날짜 지정 시 해당 날짜만, 없으면 전체 조회
        if (workDate != null) {
            schedules = scheduleRepository.findByStore_IdAndWorkDate(storeId, workDate);
        } else {
            schedules = scheduleRepository.findByStore_IdOrderByWorkDateDesc(storeId);
        }

        List<ScheduleResponse> scheduleResponses = schedules.stream()
                .map(ScheduleResponse::from)
                .toList();

        return new ScheduleListResponse(scheduleResponses);
    }
}
