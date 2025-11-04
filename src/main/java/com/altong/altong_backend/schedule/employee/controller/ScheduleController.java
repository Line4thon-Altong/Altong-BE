package com.altong.altong_backend.schedule.employee.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.schedule.dto.employee.response.ScheduleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

    @GetMapping
    public ResponseEntity<String> ok(){
        return ResponseEntity.ok("ok");
    }
}
