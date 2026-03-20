package com.api.BetStrat.util;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue historicLastMatchesQueue() {
        return new Queue("historic_last_matches", true);
    }


}
