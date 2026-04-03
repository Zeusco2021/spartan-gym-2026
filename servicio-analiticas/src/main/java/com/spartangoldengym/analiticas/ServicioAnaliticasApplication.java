package com.spartangoldengym.analiticas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.analiticas", "com.spartangoldengym.common"})
@EnableScheduling
public class ServicioAnaliticasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioAnaliticasApplication.class, args);
    }
}
