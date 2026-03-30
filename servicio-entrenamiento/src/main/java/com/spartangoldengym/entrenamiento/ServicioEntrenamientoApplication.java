package com.spartangoldengym.entrenamiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.entrenamiento", "com.spartangoldengym.common"})
public class ServicioEntrenamientoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioEntrenamientoApplication.class, args);
    }
}
