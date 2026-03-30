package com.spartangoldengym.gimnasio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.gimnasio", "com.spartangoldengym.common"})
@EnableScheduling
public class ServicioGimnasioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioGimnasioApplication.class, args);
    }
}
