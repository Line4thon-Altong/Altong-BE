package com.altong.altong_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class AltongBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AltongBackendApplication.class, args);
	}

    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
