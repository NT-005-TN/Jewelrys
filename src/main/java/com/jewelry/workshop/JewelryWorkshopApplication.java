package com.jewelry.workshop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.transaction.annotation.EnableTransactionManagement(order = 0)
@SpringBootApplication
public class JewelryWorkshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(JewelryWorkshopApplication.class, args);
        log.info("✅ API для ювелирной мастерской запущено!");
    }
}
