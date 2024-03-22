package org.example.configs;

import org.example.application.SimpleStateMachineWithUndoForkStateInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.List;

@Configuration
@EnableStateMachine
public class SimpleStateMachineWithUndoForkStateConfiguration extends StateMachineConfigurerAdapter<String, String> {

    @Override
    public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
        states.withStates()
                .initial("I")
                .fork("FORK")
                .join("JOIN")
                .state("E")
                .and().withStates()
                .parent("FORK")
                .initial("R1")
                .end("R2")
                .and().withStates()
                .parent("FORK")
                .initial("L1")
                .end("L2");
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
        transitions
                .withExternal().source("I").target("FORK").event("ItoFORK")
                .and()
                .withFork().source("FORK").target("L1").target("R1")
                .and()
                .withExternal().source("L1").target("L2").event("TIK")
                .and()
                .withExternal().source("L2").target("L1").event("TOK")
                .and()
                .withExternal().source("R1").target("R2").event("FUZ")
                .and()
                .withExternal().source("R2").target("R1").event("BAZ")
                .and()
                .withJoin().sources(List.of("R2", "L2")).target("JOIN")
                .and()
                .withExternal().source("JOIN").target("E");
    }

    @Bean
    public SimpleStateMachineWithUndoForkStateInterceptor simpleStateMachineInterceptor() {
        return new SimpleStateMachineWithUndoForkStateInterceptor();
    }
}
