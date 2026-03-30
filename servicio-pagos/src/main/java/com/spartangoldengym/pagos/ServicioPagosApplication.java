package com.spartangoldengym.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.spartangoldengym.pagos", "com.spartangoldengym.common"})
@EnableScheduling
public class ServicioPagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioPagosApplication.class, args);
    }
}
