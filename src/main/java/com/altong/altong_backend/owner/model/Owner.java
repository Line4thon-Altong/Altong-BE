package com.altong.altong_backend.owner.model;

import com.altong.altong_backend.store.model.Store;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "owner")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String refreshToken;

    public void updateRefreshToken(String token) {
        this.refreshToken = token;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}