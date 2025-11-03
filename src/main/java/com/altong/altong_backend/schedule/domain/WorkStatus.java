package com.altong.altong_backend.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
public enum WorkStatus {
    SCHEDULED,
    WORKING,
    COMPLETED
}
