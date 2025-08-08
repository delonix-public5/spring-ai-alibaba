/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.graph.reactive.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeInput;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeOutput;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Enhanced reactive node action interface that supports node input/output parameters.
 * This interface provides access to previous nodes' inputs and outputs while maintaining
 * backward compatibility with the original ReactiveNodeAction.
 *
 * @author Your Name
 * @since 1.0.0
 */
@FunctionalInterface
public interface EnhancedReactiveNodeAction extends ReactiveNodeAction {

    /**
     * Apply the enhanced node action with access to node input/output history.
     *
     * @param state the current overall state
     * @param nodeInput the node input containing previous nodes' inputs and outputs
     * @return a Mono containing the node output
     */
    Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput);

    /**
     * Default implementation for backward compatibility.
     * This method extracts state updates from the enhanced output.
     *
     * @param state the current overall state
     * @return a Mono containing the updated state map
     */
    @Override
    default Mono<Map<String, Object>> apply(OverAllState state) {
        // Create empty node input for backward compatibility
        ReactiveNodeInput emptyInput = new ReactiveNodeInput("unknown");
        
        return applyEnhanced(state, emptyInput)
                .map(ReactiveNodeOutput::getStateUpdates);
    }

    /**
     * Create an enhanced reactive node action from a lambda.
     *
     * @param action the enhanced action lambda
     * @return an enhanced reactive node action
     */
    static EnhancedReactiveNodeAction of(EnhancedReactiveNodeAction action) {
        return action;
    }

    /**
     * Create an enhanced reactive node action from a simple lambda that only uses state.
     *
     * @param action the simple action lambda
     * @return an enhanced reactive node action
     */
    static EnhancedReactiveNodeAction fromSimple(ReactiveNodeAction action) {
        return (state, nodeInput) -> action.apply(state)
                .map(stateUpdates -> ReactiveNodeOutput.builder(nodeInput.getCurrentNodeId())
                        .stateUpdates(stateUpdates)
                        .build());
    }

    /**
     * Create an enhanced reactive node action that only produces outputs without state updates.
     *
     * @param nodeId the node ID
     * @param outputProducer the function that produces outputs
     * @return an enhanced reactive node action
     */
    static EnhancedReactiveNodeAction outputOnly(String nodeId, 
            java.util.function.BiFunction<OverAllState, ReactiveNodeInput, Mono<Map<String, Object>>> outputProducer) {
        return (state, nodeInput) -> outputProducer.apply(state, nodeInput)
                .map(outputs -> ReactiveNodeOutput.builder(nodeId)
                        .outputs(outputs)
                        .build());
    }

    /**
     * Create an enhanced reactive node action that produces both outputs and state updates.
     *
     * @param nodeId the node ID
     * @param processor the function that processes inputs and produces outputs and state updates
     * @return an enhanced reactive node action
     */
    static EnhancedReactiveNodeAction withOutputsAndState(String nodeId,
            java.util.function.BiFunction<OverAllState, ReactiveNodeInput, Mono<ReactiveNodeOutput>> processor) {
        return processor::apply;
    }
}
