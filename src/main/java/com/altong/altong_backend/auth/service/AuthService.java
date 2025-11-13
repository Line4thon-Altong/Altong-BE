package com.altong.altong_backend.auth.service;

import com.altong.altong_backend.auth.dto.request.SignupRequest;
import com.altong.altong_backend.auth.dto.response.SignupResponse;
import com.altong.altong_backend.auth.dto.response.UserInfoResponse;
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
import org.springframework.security.core.Authentication;
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

    // =============== 회원가입 ===============
    public SignupResponse signup(SignupRequest req) {
        String role = req.getRole().toUpperCase();

        if (role.equals("OWNER")) return signupOwner(req);
        if (role.equals("EMPLOYEE")) return signupEmployee(req);

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private SignupResponse signupOwner(SignupRequest req) {
        if (ownerRepo.existsByUsername(req.getUsername()))
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);

        Owner owner = Owner.builder()
            .username(req.getUsername())
            .password(encoder.encode(req.getPassword()))
            .createdAt(LocalDateTime.now())
            .build();

        owner = ownerRepo.save(owner);

        Store store = Store.builder()
            .name(req.getStoreName())
            .owner(owner)
            .build();

        store = storeRepo.save(store);

        return SignupResponse.builder()
            .id(owner.getId())
            .username(owner.getUsername())
            .role("OWNER")
            .storeId(store.getId())
            .storeName(store.getName())
            .createdAt(owner.getCreatedAt())
            .build();
    }

    private SignupResponse signupEmployee(SignupRequest req) {
        if (empRepo.existsByUsername(req.getUsername()))
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);

        Employee emp = Employee.builder()
            .username(req.getUsername())
            .password(encoder.encode(req.getPassword()))
            .name(req.getName())
            .store(null)
            .createdAt(LocalDateTime.now())
            .build();

        emp = empRepo.save(emp);

        return SignupResponse.builder()
            .id(emp.getId())
            .username(emp.getUsername())
            .role("EMPLOYEE")
            .storeId(null)
            .storeName(null)
            .createdAt(emp.getCreatedAt())
            .build();
    }

    // =============== Token 재발급 ===============
    public String refreshAccessToken(String refreshToken) {
        try {
            Jws<Claims> claims = jwt.parse(refreshToken);
            Claims body = claims.getBody();
            String subject = body.getSubject();
            String role = body.get("role", String.class);
            return jwt.createAccessToken(subject, role);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    // =============== 내 정보 조회 ===============
    public UserInfoResponse getCurrentUserInfo(Authentication auth) {

        String principal = auth.getName();   // ex: OWNER:3
        String[] parts = principal.split(":");
        String role = parts[0];
        Long userId = Long.parseLong(parts[1]);

        // ================================
        // OWNER
        // ================================
        if (role.equals("OWNER")) {

            Owner owner = ownerRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

            Store store = storeRepo.findByOwner(owner)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

            return UserInfoResponse.builder()
                .id(owner.getId())
                .username(owner.getUsername())
                .displayName(store.getName())     // 사장 = 가게명
                .storeId(store.getId())
                .storeName(store.getName())
                .role("OWNER")
                .build();
        }

        // ================================
        // EMPLOYEE
        // ================================
        Employee emp = empRepo.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        Store store = emp.getStore();

        return UserInfoResponse.builder()
            .id(emp.getId())
            .username(emp.getUsername())
            .displayName(emp.getName())          // 알바 = 본명
            .storeId(store != null ? store.getId() : null)
            .storeName(store != null ? store.getName() : null)
            .role("EMPLOYEE")
            .build();
    }
}