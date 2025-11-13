package com.altong.altong_backend.employee.service;

import com.altong.altong_backend.employee.dto.request.*;
import com.altong.altong_backend.employee.dto.response.*;
import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAuthService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder passwordEncoder;

    // ================================
    // 1. 로그인
    // ================================
    public EmployeeLoginResponse login(EmployeeLoginRequest req) {

        Employee emp = employeeRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(req.getPassword(), emp.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String subject = "EMPLOYEE:" + emp.getId();
        String access = jwt.createAccessToken(subject, "EMPLOYEE");
        String refresh = jwt.createRefreshToken(subject);

        emp.updateRefreshToken(refresh);
        employeeRepository.save(emp);

        Store store = emp.getStore();

        return EmployeeLoginResponse.builder()
            .id(emp.getId())
            .username(emp.getUsername())
            .name(emp.getName())
            .displayName(emp.getName()) // 전체 통일
            .storeId(store != null ? store.getId() : null)
            .storeName(store != null ? store.getName() : null)
            .role("EMPLOYEE")
            .accessToken(access)
            .refreshToken(refresh)
            .build();
    }

    // ================================
    // 2. 비밀번호 변경
    // ================================
    public EmployeePasswordUpdateResponse updatePassword(Long employeeId, EmployeePasswordUpdateRequest req) {

        Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(req.getOldPassword(), emp.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        emp.updatePassword(passwordEncoder.encode(req.getNewPassword()));
        employeeRepository.save(emp);

        return new EmployeePasswordUpdateResponse("비밀번호 변경 완료");
    }

    // ================================
    // 3. 가게 연동 해제
    // ================================
    public EmployeeUnlinkStoreResponse unlinkStore(Long employeeId) {

        Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        emp.updateStore(null);
        employeeRepository.save(emp);

        return new EmployeeUnlinkStoreResponse(emp.getId(), null, "가게 연동이 해제되었습니다.");
    }

    // ================================
    // 4. 로그아웃
    // ================================
    public EmployeeLogoutResponse logout(EmployeeLogoutRequest req) {

        String token = req.getRefreshToken();

        String subject = jwt.parse(token).getBody().getSubject(); // EMPLOYEE:3

        if (!subject.startsWith("EMPLOYEE:")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long empId = Long.parseLong(subject.substring("EMPLOYEE:".length()));

        Employee emp = employeeRepository.findById(empId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        emp.updateRefreshToken(null);
        employeeRepository.save(emp);

        return new EmployeeLogoutResponse("로그아웃 완료");
    }
}