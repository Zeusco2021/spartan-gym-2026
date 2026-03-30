package com.spartangoldengym.analiticas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.analiticas", "com.spartangoldengym.common"})
public class ServicioAnaliticasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioAnaliticasApplication.class, args);
    }
}
