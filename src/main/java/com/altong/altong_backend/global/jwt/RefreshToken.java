package com.altong.altong_backend.global.jwt;

import lombok.*;
        import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String jti; // 토큰 고유 ID

    @Indexed
    private String username; // 사용자 이름

    @TimeToLive
    private Long ttl; // 만료 시간
}
