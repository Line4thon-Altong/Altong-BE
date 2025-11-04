package com.altong.altong_backend.schedule.entity;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="schedule_id")
    private Long id;

    @Column(nullable = false)
    private LocalDate workDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private WorkStatus workStatus;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="employee_id",nullable=false)
    private Employee employee;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="store_id",nullable=false)
    private Store store;


}
