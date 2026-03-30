package com.spartangoldengym.nutricion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.nutricion", "com.spartangoldengym.common"})
public class ServicioNutricionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioNutricionApplication.class, args);
    }
}
