package com.altong.altong_backend.store.model;

import com.altong.altong_backend.owner.model.Owner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 가게 이름

    @Column(length = 50)
    private String category;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

}
