package com.spartangoldengym.calendario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.calendario", "com.spartangoldengym.common"})
public class ServicioCalendarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioCalendarioApplication.class, args);
    }
}
