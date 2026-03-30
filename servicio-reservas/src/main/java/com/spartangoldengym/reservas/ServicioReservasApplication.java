package com.spartangoldengym.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.reservas", "com.spartangoldengym.common"})
public class ServicioReservasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioReservasApplication.class, args);
    }
}
