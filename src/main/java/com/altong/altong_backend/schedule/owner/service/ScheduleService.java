package com.altong.altong_backend.schedule.owner.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.schedule.owner.dto.request.ScheduleCreateRequest;
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

@Service
@Builder
@AllArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ScheduleResponse createSchedule(Long storeId,Long employeeId,ScheduleCreateRequest scheduleCreateRequest) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Schedule schedule = Schedule.builder()
                .store(store)
                .employee(employee)
                .workDate(scheduleCreateRequest.workDate())
                .startTime(null) // 알바생 입력 필드
                .endTime(null)   // 알바생 입력 필드
                .workStatus(WorkStatus.SCHEDULED)
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        return ScheduleResponse.from(saved);
    }
}
