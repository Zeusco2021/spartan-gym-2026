package com.spartangoldengym.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.notificaciones", "com.spartangoldengym.common"})
@EnableScheduling
public class ServicioNotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioNotificacionesApplication.class, args);
    }
}
