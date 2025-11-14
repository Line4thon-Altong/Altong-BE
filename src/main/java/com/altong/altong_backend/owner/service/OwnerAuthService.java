package com.altong.altong_backend.owner.service;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.owner.dto.request.*;
import com.altong.altong_backend.owner.dto.response.*;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.store.repository.StoreRepository;
import com.altong.altong_backend.store.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerAuthService {

    private final OwnerRepository ownerRepository;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder passwordEncoder;
    private final StoreRepository storeRepository;

    // ===================================================
    // 1. 로그인
    // ===================================================
    public OwnerLoginResponse login(OwnerLoginRequest req) {

        Owner owner = ownerRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(req.getPassword(), owner.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String subject = "OWNER:" + owner.getId();
        String access = jwt.createAccessToken(subject, "OWNER");
        String refresh = jwt.createRefreshToken(subject);

        owner.updateRefreshToken(refresh);
        ownerRepository.save(owner);

        Store store = storeRepository.findByOwner(owner).orElse(null);

        // displayName: 기본적으로 가게명, 가게 없으면 username 사용
        String displayName = (store != null) ? store.getName() : owner.getUsername();

        return OwnerLoginResponse.builder()
                .id(owner.getId())
                .username(owner.getUsername())
                .displayName(displayName)
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .role("OWNER")
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    // ===================================================
    // 2. 비밀번호 변경
    // ===================================================
    public OwnerPasswordUpdateResponse updatePassword(Long ownerId, OwnerPasswordUpdateRequest req) {

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getOldPassword(), owner.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        owner.updatePassword(passwordEncoder.encode(req.getNewPassword()));
        ownerRepository.save(owner);

        return new OwnerPasswordUpdateResponse("비밀번호 변경 완료");
    }

    // ===================================================
    // 3. 상호명 변경
    // ===================================================
    public OwnerStoreNameUpdateResponse updateStoreName(Long ownerId, OwnerStoreNameUpdateRequest req) {

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        Store store = storeRepository.findByOwner(owner).orElse(null);
        if (store == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        store.updateName(req.getStoreName());

        return new OwnerStoreNameUpdateResponse("상호명이 변경되었습니다.");
    }

    // ===================================================
    // 4. 로그아웃
    // ===================================================
    public OwnerLogoutResponse logout(OwnerLogoutRequest req) {

        String token = req.getRefreshToken();

        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        token = token.trim();

        String subject;
        try {
            subject = jwt.parse(token).getBody().getSubject();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (subject == null || !subject.startsWith("OWNER:")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long ownerId = Long.parseLong(subject.substring("OWNER:".length()));

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        owner.updateRefreshToken(null);
        ownerRepository.save(owner);

        return new OwnerLogoutResponse("로그아웃 완료");
    }
}