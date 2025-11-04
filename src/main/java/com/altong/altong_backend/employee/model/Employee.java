package com.altong.altong_backend.employee.model;

import com.altong.altong_backend.schedule.domain.Schedule;
import com.altong.altong_backend.store.model.Store;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="employee",cascade=CascadeType.ALL)
    private List<Schedule> schedules;

    @Column
    private LocalDateTime addedAt;

    public Employee assignToStore(Store store) {
        return Employee.builder()
                .id(this.id)
                .name(this.name)
                .username(this.username)
                .password(this.password)
                .store(store)
                .createdAt(this.createdAt)
                .addedAt(LocalDateTime.now())
                .build();
    }

    public void updateStore(Store store) {
        this.store = store;
    }
}
