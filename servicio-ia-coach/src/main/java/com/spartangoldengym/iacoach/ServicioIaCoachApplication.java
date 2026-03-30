package com.spartangoldengym.iacoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.iacoach", "com.spartangoldengym.common"})
public class ServicioIaCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioIaCoachApplication.class, args);
    }
}
