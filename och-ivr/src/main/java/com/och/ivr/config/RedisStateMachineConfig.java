package com.och.ivr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.data.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;

@Configuration
@EnableStateMachine
@EnableRedisHttpSession
public class RedisStateMachineConfig {


    @Bean
    public RepositoryStateMachinePersist<Object, Object> stateMachinePersist(RedisConnectionFactory connectionFactory) {
        RedisStateMachineContextRepository<Object, Object> repository =
                new RedisStateMachineContextRepository<>(connectionFactory);
        return new RepositoryStateMachinePersist<>(repository);
    }

    @Bean(name = "redisStateMachinePersister")
    public RedisStateMachinePersister<Object, Object> redisStateMachinePersister(StateMachinePersist<Object, Object, String> stateMachinePersist) {
        return new RedisStateMachinePersister<>(stateMachinePersist);
    }
}
