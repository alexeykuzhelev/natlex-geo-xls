package com.natlex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaRepositories("com.natlex.repository")
public class NatlexGeoXlsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NatlexGeoXlsApplication.class, args);
    }
}
