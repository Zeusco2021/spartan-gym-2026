package com.spartangoldengym.seguimiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.seguimiento", "com.spartangoldengym.common"})
public class ServicioSeguimientoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioSeguimientoApplication.class, args);
    }
}
