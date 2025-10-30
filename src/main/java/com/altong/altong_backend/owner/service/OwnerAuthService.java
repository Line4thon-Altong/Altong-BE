package com.altong.altong_backend.owner.service;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.global.jwt.RefreshToken;
import com.altong.altong_backend.global.jwt.RefreshTokenRepository;
import com.altong.altong_backend.owner.dto.request.*;
import com.altong.altong_backend.owner.dto.response.*;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerAuthService {

    private final OwnerRepository ownerRepo;
    private final StoreRepository storeRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final RefreshTokenRepository refreshRepo;

    /** 로그인 */
    public OwnerLoginResponse login(OwnerLoginRequest req) {
        Owner owner = ownerRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!encoder.matches(req.getPassword(), owner.getPassword()))
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);

        String at = jwt.createAccessToken("OWNER:" + owner.getId(), "OWNER");
        String rt = jwt.createRefreshToken("OWNER:" + owner.getId());

        // Redis 저장
        RefreshToken token = RefreshToken.builder()
                .jti(UUID.randomUUID().toString())
                .username(owner.getUsername())
                .ttl(Duration.ofHours(24).toSeconds())
                .build();
        refreshRepo.save(token);

        return new OwnerLoginResponse(at, rt);
    }

    /** 비밀번호 변경 */
    public OwnerPasswordUpdateResponse updatePassword(Long ownerId, OwnerPasswordUpdateRequest req) {
        Owner owner = ownerRepo.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (!encoder.matches(req.getOldPassword(), owner.getPassword()))
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);

        owner = Owner.builder()
                .id(owner.getId())
                .username(owner.getUsername())
                .password(encoder.encode(req.getNewPassword()))
                .createdAt(owner.getCreatedAt())
                .build();

        ownerRepo.save(owner);
        return new OwnerPasswordUpdateResponse("비밀번호가 변경되었습니다.");
    }

    /** 상호명 변경 */
    public OwnerPasswordUpdateResponse updateStoreName(Long ownerId, OwnerStoreNameUpdateRequest req) {
        List<Store> stores = storeRepo.findAll();
        Store store = stores.stream()
                .filter(s -> s.getOwner() != null && s.getOwner().getId().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        Store updated = Store.builder()
                .id(store.getId())
                .name(req.getStoreName())
                .category(store.getCategory())
                .owner(store.getOwner())
                .build();

        storeRepo.save(updated);
        return new OwnerPasswordUpdateResponse("상호명이 변경되었습니다.");
    }

    /** 로그아웃 */
    public OwnerLogoutResponse logout(OwnerLogoutRequest req) {
        try {
            var parsed = jwt.parse(req.getRefreshToken());
            String subject = parsed.getBody().getSubject();
            if (subject.startsWith("OWNER:")) subject = subject.substring(6);
            refreshRepo.deleteByUsername(subject);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return new OwnerLogoutResponse("로그아웃 되었습니다.");
    }
}
