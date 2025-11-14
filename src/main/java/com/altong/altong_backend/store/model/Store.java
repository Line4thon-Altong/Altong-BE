package com.altong.altong_backend.store.model;

import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.schedule.model.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(mappedBy="store",cascade=CascadeType.ALL)
    private List<Schedule> schedules;

    public void updateName(String newName) {
        this.name = newName;
    }
}
