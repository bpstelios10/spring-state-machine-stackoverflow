package org.example.application;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;

import java.util.logging.Logger;

public class SimpleStateMachineLogTransitionInterceptor extends StateMachineInterceptorAdapter<String, String> {

    private static final Logger log = Logger.getLogger(SimpleStateMachineLogTransitionInterceptor.class.getName());

    @Override
    public StateContext<String, String> postTransition(StateContext<String, String> stateContext) {
        log.info("Transition was completed. New state: [" + stateContext.getTarget().getId() + "]");

        return stateContext;
    }
}
