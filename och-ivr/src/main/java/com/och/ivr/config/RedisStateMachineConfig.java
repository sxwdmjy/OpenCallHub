package com.och.ivr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.data.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;

@Configuration
@EnableStateMachine
@EnableRedisHttpSession
public class RedisStateMachineConfig{


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public StateMachinePersist<String, String, String> stateMachinePersist(RedisConnectionFactory connectionFactory) {
        RedisStateMachineContextRepository<String, String> repository =
                new RedisStateMachineContextRepository<String, String>(connectionFactory);
        return new RepositoryStateMachinePersist<String, String>(repository);
    }

    @Bean
    public RedisStateMachinePersister<String, String> redisStateMachinePersister(StateMachinePersist<String, String, String> stateMachinePersist) {
        return new RedisStateMachinePersister<>(stateMachinePersist);
    }
}
