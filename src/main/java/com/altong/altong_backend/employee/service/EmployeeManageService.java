package com.altong.altong_backend.employee.service;

import com.altong.altong_backend.employee.dto.request.EmployeeAddRequest;
import com.altong.altong_backend.employee.dto.response.EmployeeAddResponse;
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

        // 알바생 - 가게 연결
        emp = Employee.builder()
                .id(emp.getId())
                .name(emp.getName())
                .username(emp.getUsername())
                .password(emp.getPassword())
                .store(store)
                .createdAt(emp.getCreatedAt())
                .build();

        employeeRepo.save(emp);

        // 응답 반환
        return new EmployeeAddResponse("알바생을 추가했습니다.");
    }
}
