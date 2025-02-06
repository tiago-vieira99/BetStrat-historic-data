package com.api.BetStrat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableCaching
public class BetStratHistoricDataApplication {

	public static void main(String[] args) {

		log.info("Started BetStrat-HistoricalData App !");
		SpringApplication.run(BetStratHistoricDataApplication.class, args);

	}

}
