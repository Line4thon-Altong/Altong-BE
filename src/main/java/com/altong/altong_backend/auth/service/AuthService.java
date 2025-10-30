package com.altong.altong_backend.auth.service;

import com.altong.altong_backend.auth.dto.request.SignupRequest;
import com.altong.altong_backend.auth.dto.response.SignupResponse;
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
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OwnerRepository ownerRepo;
    private final EmployeeRepository empRepo;
    private final StoreRepository storeRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    /** 회원가입 */
    public SignupResponse signup(SignupRequest req) {
        String role = req.getRole().toUpperCase();

        // 사장 회원가입
        if ("OWNER".equals(role)) {
            if (ownerRepo.existsByUsername(req.getUsername())) {
                throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
            }

            // Owner 저장
            Owner owner = Owner.builder()
                    .username(req.getUsername())
                    .password(encoder.encode(req.getPassword()))
                    .createdAt(LocalDateTime.now())
                    .build();
            owner = ownerRepo.save(owner);

            // Store 연결 후 저장
            if (req.getStoreName() == null || req.getStoreName().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            Store store = Store.builder()
                    .name(req.getStoreName())
                    .category(null)
                    .owner(owner)
                    .build();
            storeRepo.save(store);

            return new SignupResponse(owner.getId(), owner.getUsername(), "OWNER", owner.getCreatedAt());
        }

        // 알바 회원가입
        else if ("EMPLOYEE".equals(role)) {
            if (empRepo.existsByUsername(req.getUsername())) {
                throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
            }

            if (req.getName() == null || req.getName().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            Employee emp = Employee.builder()
                    .name(req.getName())
                    .username(req.getUsername())
                    .password(encoder.encode(req.getPassword()))
                    .store(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            empRepo.save(emp);
            return new SignupResponse(emp.getId(), emp.getUsername(), "EMPLOYEE", emp.getCreatedAt());
        }

        // ROLE 잘못 입력 시
        else {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /** JWT 재발급 */
    public String refreshAccessToken(String refreshToken) {
        try {
            Jws<Claims> claims = jwt.parse(refreshToken);
            String subject = claims.getBody().getSubject();
            String role = subject.startsWith("OWNER") ? "OWNER" : "EMPLOYEE";
            return jwt.createAccessToken(subject, role);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
