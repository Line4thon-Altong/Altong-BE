package com.altong.altong_backend.employee.service;

import com.altong.altong_backend.employee.dto.request.*;
import com.altong.altong_backend.employee.dto.response.*;
import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.global.jwt.RefreshToken;
import com.altong.altong_backend.global.jwt.RefreshTokenRepository;
import com.altong.altong_backend.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeAuthService {
    private final EmployeeRepository empRepo;
    private final StoreRepository storeRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final RefreshTokenRepository refreshRepo;

    /** 로그인 */
    public EmployeeLoginResponse login(EmployeeLoginRequest req) {
        Employee emp = empRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!encoder.matches(req.getPassword(), emp.getPassword()))
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);

        String at = jwt.createAccessToken("EMPLOYEE:" + emp.getId(), "EMPLOYEE");
        String rt = jwt.createRefreshToken("EMPLOYEE:" + emp.getId());

        RefreshToken token = RefreshToken.builder()
                .jti(UUID.randomUUID().toString())
                .username(emp.getUsername())
                .ttl(Duration.ofHours(24).toSeconds())
                .build();
        refreshRepo.save(token);

        return new EmployeeLoginResponse(at, rt);
    }

    /** 로그아웃 */
    public EmployeeLogoutResponse logout(EmployeeLogoutRequest req) {
        try {
            var parsed = jwt.parse(req.getRefreshToken());
            String subject = parsed.getBody().getSubject();
            if (subject.startsWith("EMPLOYEE:")) subject = subject.substring(9);
            refreshRepo.deleteByUsername(subject);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return new EmployeeLogoutResponse("로그아웃 되었습니다.");
    }

    /** 비밀번호 변경 */
    public EmployeePasswordUpdateResponse updatePassword(Long empId, EmployeePasswordUpdateRequest req) {
        Employee emp = empRepo.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (!encoder.matches(req.getOldPassword(), emp.getPassword()))
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);

        emp = Employee.builder()
                .id(emp.getId())
                .name(emp.getName())
                .username(emp.getUsername())
                .password(encoder.encode(req.getNewPassword()))
                .store(emp.getStore())
                .createdAt(emp.getCreatedAt())
                .build();

        empRepo.save(emp);
        return new EmployeePasswordUpdateResponse("비밀번호가 변경되었습니다.");
    }

    /** 가게 연동 해제 */
    public EmployeePasswordUpdateResponse unlinkStore(EmployeeUnlinkStoreRequest req) {
        Employee emp = empRepo.findById(req.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        emp = Employee.builder()
                .id(emp.getId())
                .name(emp.getName())
                .username(emp.getUsername())
                .password(emp.getPassword())
                .store(null)
                .createdAt(emp.getCreatedAt())
                .build();

        empRepo.save(emp);
        return new EmployeePasswordUpdateResponse("가게 연동이 해제되었습니다.");
    }
}
