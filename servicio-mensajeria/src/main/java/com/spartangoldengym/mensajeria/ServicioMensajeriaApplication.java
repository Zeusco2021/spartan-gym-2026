package com.spartangoldengym.mensajeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.mensajeria", "com.spartangoldengym.common"})
public class ServicioMensajeriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioMensajeriaApplication.class, args);
    }
}
