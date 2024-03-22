package org.example.configs;

import org.example.application.SimpleStateMachineWithUndoForkStateInterceptor;
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
@ContextConfiguration(classes = {SimpleStateMachineWithUndoForkStateConfiguration.class})
class SimpleStateMachineWithUndoForkStateConfigurationTest {

    @Autowired
    private StateMachine<String, String> stateMachine;
    @Autowired
    private SimpleStateMachineWithUndoForkStateInterceptor undoForkStateInterceptor;

    @BeforeEach
    void resetStateMachineAndAddLoggingInterceptor() {
        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(undoForkStateInterceptor);
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>("I", null, null, null));
                });
        stateMachine.start();
    }

    @Test
    public void whenStateMachineEventsCompletesFirstRegionAndThenGoBack_thenDoesNotReachEndState() {
        assertThat("I").isEqualTo(stateMachine.getState().getId());

        stateMachine.sendEvent("ItoFORK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L2", "R1");
        stateMachine.sendEvent("TOK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R2");

        // join still succeeds after undo
        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("E");
    }

    @Test
    public void whenStateMachineEventsCompletesSecondRegionAndThenGoBack_thenDoesNotReachEndState() {
        assertThat("I").isEqualTo(stateMachine.getState().getId());

        stateMachine.sendEvent("ItoFORK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R2");
        stateMachine.sendEvent("BAZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L2", "R1");

        // join still succeeds after undo
        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("E");
    }

    @Test
    public void whenStateMachineEventsBothComplete_thenReachEndState() {
        assertThat("I").isEqualTo(stateMachine.getState().getId());

        stateMachine.sendEvent("ItoFORK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R2");

        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("E");
    }

    @Test
    public void whenSomeUndosAndThenEndBothRegions_thenReachEndState() {
        assertThat("I").isEqualTo(stateMachine.getState().getId());

        stateMachine.sendEvent("ItoFORK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L2", "R1");
        stateMachine.sendEvent("TOK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R2");
        stateMachine.sendEvent("BAZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L1", "R1");

        // join still succeeds after undo
        stateMachine.sendEvent("TIK");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("FORK", "L2", "R1");
        stateMachine.sendEvent("FUZ");
        assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder("E");
    }
}