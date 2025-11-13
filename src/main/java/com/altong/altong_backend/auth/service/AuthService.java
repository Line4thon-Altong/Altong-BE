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

        // 1) Owner 저장
        Owner owner = Owner.builder()
            .username(req.getUsername())
            .password(encoder.encode(req.getPassword()))
            .createdAt(LocalDateTime.now())
            .build();
        owner = ownerRepo.save(owner);

        // 2) Store 생성
        Store store = Store.builder()
            .name(req.getStoreName())
            .owner(owner)   // Store → Owner 참조
            .build();
        store = storeRepo.save(store);

        // 3) Owner.store 연결
        owner.updateStore(store);
        ownerRepo.save(owner);

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
            String subject = body.getSubject();  // ex: OWNER:3
            String role = body.get("role", String.class);
            return jwt.createAccessToken(subject, role);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    // =============== 내 정보 조회 ===============
    public UserInfoResponse getCurrentUserInfo(Authentication auth) {

        String principal = auth.getName();

        String[] parts = principal.split(":");
        String role = parts[0];
        Long userId = Long.parseLong(parts[1]);

        if (role.equals("OWNER")) {
            Owner owner = ownerRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

            return UserInfoResponse.builder()
                .id(owner.getId())
                .username(owner.getUsername())
                .displayName(owner.getStore().getName()) // 사장 = 가게명
                .storeId(owner.getStore().getId())
                .storeName(owner.getStore().getName())
                .role("OWNER")
                .build();
        }

        Employee emp = empRepo.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        return UserInfoResponse.builder()
            .id(emp.getId())
            .username(emp.getUsername())
            .displayName(emp.getName()) // 알바 = 본명
            .storeId(emp.getStore() != null ? emp.getStore().getId() : null)
            .storeName(emp.getStore() != null ? emp.getStore().getName() : null)
            .role("EMPLOYEE")
            .build();
    }

    private UserInfoResponse getEmployeeInfo(Long id) {
        Employee emp = empRepo.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Store store = emp.getStore();

        return UserInfoResponse.builder()
            .id(emp.getId())
            .username(emp.getUsername())
            .displayName(emp.getName())
            .storeId(store != null ? store.getId() : null)
            .storeName(store != null ? store.getName() : null)
            .role("EMPLOYEE")
            .build();
    }
}