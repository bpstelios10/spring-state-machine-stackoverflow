package org.example.configs;

import org.example.application.SimpleStateMachineLogTransitionInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SimpleStateMachineWithoutUndoOnForkStatesConfiguration.class})
class SimpleStateMachineWithoutUndoOnForkStatesConfigurationTest {
    @Autowired
    private StateMachine<String, String> stateMachine;
    @Autowired
    private SimpleStateMachineLogTransitionInterceptor logTransitionInterceptor;

    @BeforeEach
    void resetStateMachineAndAddLoggingInterceptor() {
        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(logTransitionInterceptor);
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>("I", null, null, null));
                });
        stateMachine.start();
    }

    @Test
    public void whenStateMachineEventsCompletesOneRegionAndThenGoBack_thenStillReachesEndState() {
        assertThat("I").isEqualTo(stateMachine.getState().getId());

        stateMachine.sendEvent("ItoFORK");
        assertThat(stateMachine.getState().getIds()).containsExactly("LR", "L1", "R1");

        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactly("LR", "L2", "R1");
        stateMachine.sendEvent("TOK");
        assertThat(stateMachine.getState().getIds()).containsExactly("LR", "L1", "R1");

        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactly("E");
    }
}
