package com.jewelry.workshop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class JewelryWorkshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(JewelryWorkshopApplication.class, args);
        log.info("✅ API для ювелирной мастерской запущено!");
    }
}
