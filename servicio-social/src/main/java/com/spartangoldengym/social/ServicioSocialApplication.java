package com.spartangoldengym.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.social", "com.spartangoldengym.common"})
public class ServicioSocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioSocialApplication.class, args);
    }
}
