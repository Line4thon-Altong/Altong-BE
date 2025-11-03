package com.altong.altong_backend.employee.service;

import com.altong.altong_backend.employee.dto.request.EmployeeAddRequest;
import com.altong.altong_backend.employee.dto.response.EmployeeAddResponse;
import com.altong.altong_backend.employee.dto.response.EmployeeListResponse;
import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeManageService {

    private final JwtTokenProvider jwt;
    private final OwnerRepository ownerRepo;
    private final StoreRepository storeRepo;
    private final EmployeeRepository employeeRepo;

    // 알바생 추가
    @Transactional
    public EmployeeAddResponse addEmployee(String token, EmployeeAddRequest req) {
        // JWT 토큰 파싱
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

        // 사장님 조회
        Owner owner = ownerRepo.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        // 가게 조회
        Store store = storeRepo.findByOwner(owner)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 알바생 존재 확인
        Employee emp = employeeRepo.findByUsername(req.getEmployeeUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Employee updated = emp.assignToStore(store);
        employeeRepo.save(updated);

        return new EmployeeAddResponse("알바생을 추가했습니다.");
    }

    // 알바생 목록 조회
    @Transactional(readOnly = true)
    public List<EmployeeListResponse> getEmployees(String token) {
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

        Owner owner = ownerRepo.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        Store store = storeRepo.findByOwner(owner)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        return employeeRepo.findByStore(store).stream()
                .sorted(Comparator.comparing(Employee::getAddedAt).reversed())
                .map(emp -> EmployeeListResponse.builder()
                        .id(emp.getId())
                        .username(emp.getUsername())
                        .name(emp.getName())
                        .addedAt(emp.getAddedAt())
                        .build())
                .toList();
    }
}
