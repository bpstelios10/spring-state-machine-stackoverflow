package org.example.application;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;

import java.util.logging.Logger;

public class SimpleStateMachineWithUndoForkStateInterceptor extends StateMachineInterceptorAdapter<String, String> {

    private static final Logger log = Logger.getLogger(SimpleStateMachineWithUndoForkStateInterceptor.class.getName());

    @Override
    public StateContext<String, String> postTransition(StateContext<String, String> stateContext) {
        log.info("Transition was completed. New state: [" + stateContext.getTarget().getId() + "]");

        String event = stateContext.getTransition().getTrigger().getEvent();
        if ("TOK".equals(event)) {
            StateMachine<String, String> stateMachine = stateContext.getStateMachine();
            if (!stateMachine.getState().getIds().contains("E") && stateMachine.getState().getIds().contains("R1")) {
                resetState(stateMachine);
            }
        } else if ("BAZ".equals(event)) {
            StateMachine<String, String> stateMachine = stateContext.getStateMachine();
            if (!stateMachine.getState().getIds().contains("E") && stateMachine.getState().getIds().contains("L1")) {
                resetState(stateMachine);
            }
        }

        return stateContext;
    }

    private void resetState(StateMachine<String, String> stateMachine) {
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(this);
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>("FORK", null, null, null));
                });
    }
}
