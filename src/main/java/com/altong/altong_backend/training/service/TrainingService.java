package com.altong.altong_backend.training.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import com.altong.altong_backend.training.dto.response.EmployeeDashboardResponse;
import com.altong.altong_backend.training.dto.response.OwnerDashboardResponse;
import com.altong.altong_backend.training.repository.TrainingRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final StoreRepository storeRepository;
    private final OwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwt;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 사장님용 대시보드 조회
    @Transactional(readOnly = true)
    public OwnerDashboardResponse getOwnerDashboard(String token) {
        String accessToken = token.replace("Bearer ", "");
        Claims claims;
        try {
            claims = jwt.parse(accessToken).getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String subject = claims.getSubject();
        if (!subject.startsWith("OWNER:")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ROLE);
        }
        Long ownerId = Long.parseLong(subject.substring(6));

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        Store store = storeRepository.findByOwner(owner)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        int employeeCount = employeeRepository.countByStore(store);

        List<OwnerDashboardResponse.TrainingSummary> trainings =
                trainingRepository.findByStore(store).stream()
                        .map(t -> OwnerDashboardResponse.TrainingSummary.builder()
                                .id(t.getId())
                                .title(t.getTitle())
                                .createdAt(t.getCreatedAt().format(FORMATTER))
                                .build())
                        .collect(Collectors.toList());

        return OwnerDashboardResponse.builder()
                .employeeCount(employeeCount)
                .trainings(trainings)
                .build();
    }

    // 알바생용 대시보드 조회
    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getEmployeeDashboard(String token) {
        String accessToken = token.replace("Bearer ", "");
        Claims claims;
        try {
            claims = jwt.parse(accessToken).getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String subject = claims.getSubject();
        if (!subject.startsWith("EMPLOYEE:")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ROLE);
        }
        Long employeeId = Long.parseLong(subject.substring(9));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Store store = employee.getStore();
        if (store == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        List<EmployeeDashboardResponse.TrainingSummary> trainings =
                trainingRepository.findByStore(store).stream()
                        .map(t -> EmployeeDashboardResponse.TrainingSummary.builder()
                                .id(t.getId())
                                .title(t.getTitle())
                                .createdAt(t.getCreatedAt().format(FORMATTER))
                                .build())
                        .collect(Collectors.toList());

        return EmployeeDashboardResponse.builder()
                .trainings(trainings)
                .build();
    }
}