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
package com.alibaba.cloud.ai.graph.reactive;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveEdgeAction;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveNodeAction;
import com.alibaba.cloud.ai.graph.reactive.action.EnhancedReactiveNodeAction;
import com.alibaba.cloud.ai.graph.reactive.context.ReactiveNodeExecutionContext;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeInput;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Reactive compiled graph that can execute workflows asynchronously.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveCompiledGraph {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveCompiledGraph.class);

    private final ReactiveStateGraph graph;
    private int maxIterations = 100;
    private final ReactiveNodeExecutionContext executionContext;

    public ReactiveCompiledGraph(ReactiveStateGraph graph) {
        this.graph = graph;
        this.executionContext = new ReactiveNodeExecutionContext();
    }

    /**
     * Set the maximum number of iterations to prevent infinite loops.
     *
     * @param maxIterations the maximum iterations
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Get the execution context.
     *
     * @return the execution context
     */
    public ReactiveNodeExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Clear the execution context.
     */
    public void clearExecutionContext() {
        executionContext.clear();
    }

    /**
     * Invoke the graph with input data and return the final state.
     *
     * @param inputs the input data
     * @return a Mono containing the final state
     */
    public Mono<OverAllState> invoke(Map<String, Object> inputs) {
        return createInitialState(inputs)
                .flatMap(this::executeGraph);
    }

    /**
     * Stream the execution of the graph, emitting intermediate states.
     *
     * @param inputs the input data
     * @return a Flux of intermediate states
     */
    public Flux<OverAllState> stream(Map<String, Object> inputs) {
        return createInitialState(inputs)
                .flatMapMany(this::streamGraph);
    }

    private Mono<OverAllState> createInitialState(Map<String, Object> inputs) {
        return Mono.fromCallable(() -> {
            OverAllState state = new OverAllState();
            // Register key strategies
            Map<String, com.alibaba.cloud.ai.graph.KeyStrategy> strategies = graph.getStateFactory().get();
            strategies.forEach(state::registerKeyAndStrategy);
            // Set initial inputs
            state.updateState(inputs);
            
            return state;
        });
    }

    private Mono<OverAllState> executeGraph(OverAllState initialState) {
        return executeNode(ReactiveStateGraph.START, initialState, 0)
                .doOnSuccess(state -> logger.info("Graph execution completed successfully"))
                .doOnError(error -> logger.error("Graph execution failed", error));
    }

    private Flux<OverAllState> streamGraph(OverAllState initialState) {
        return streamNode(ReactiveStateGraph.START, initialState, 0)
                .doOnComplete(() -> logger.info("Graph streaming completed"))
                .doOnError(error -> logger.error("Graph streaming failed", error));
    }

    private Mono<OverAllState> executeNode(String currentNode, OverAllState state, int iteration) {
        if (iteration >= maxIterations) {
            return Mono.error(new RuntimeException("Maximum iterations exceeded: " + maxIterations));
        }

        if (ReactiveStateGraph.END.equals(currentNode)) {
            return Mono.just(state);
        }

        if (ReactiveStateGraph.START.equals(currentNode)) {
            return getNextNode(currentNode, state)
                    .flatMap(nextNode -> executeNode(nextNode, state, iteration + 1));
        }

        // Check if this is an enhanced node
        EnhancedReactiveNodeAction enhancedAction = graph.getEnhancedNodes().get(currentNode);
        if (enhancedAction != null) {
            return executeEnhancedNode(currentNode, enhancedAction, state, iteration);
        }

        // Fall back to regular node execution
        ReactiveNodeAction nodeAction = graph.getNodes().get(currentNode);
        if (nodeAction == null) {
            return Mono.error(new RuntimeException("Node not found: " + currentNode));
        }

        return nodeAction.apply(state)
                .map(updates -> {
                    state.updateState(updates);
                    return state;
                })
                .flatMap(updatedState -> getNextNode(currentNode, updatedState))
                .flatMap(nextNode -> executeNode(nextNode, state, iteration + 1));
    }

    private Mono<OverAllState> executeEnhancedNode(String nodeId, EnhancedReactiveNodeAction action,
                                                  OverAllState state, int iteration) {
        // Create node input with current state and execution history
        Map<String, Object> directInputs = new HashMap<>(state.data());
        ReactiveNodeInput nodeInput = executionContext.createNodeInput(nodeId, directInputs);

        return action.applyEnhanced(state, nodeInput)
                .doOnNext(nodeOutput -> {
                    // Record the execution in context
                    executionContext.recordNodeExecution(nodeId, nodeInput, nodeOutput);
                })
                .map(nodeOutput -> {
                    // Apply state updates
                    state.updateState(nodeOutput.getStateUpdates());
                    return state;
                })
                .flatMap(updatedState -> getNextNode(nodeId, updatedState))
                .flatMap(nextNode -> executeNode(nextNode, state, iteration + 1));
    }

    private Flux<OverAllState> streamNode(String currentNode, OverAllState state, int iteration) {
        if (iteration >= maxIterations) {
            return Flux.error(new RuntimeException("Maximum iterations exceeded: " + maxIterations));
        }

        if (ReactiveStateGraph.END.equals(currentNode)) {
            return Flux.just(state);
        }

        if (ReactiveStateGraph.START.equals(currentNode)) {
            return getNextNode(currentNode, state)
                    .flatMapMany(nextNode -> streamNode(nextNode, state, iteration + 1));
        }

        // Check if this is an enhanced node
        EnhancedReactiveNodeAction enhancedAction = graph.getEnhancedNodes().get(currentNode);
        if (enhancedAction != null) {
            return streamEnhancedNode(currentNode, enhancedAction, state, iteration);
        }

        // Fall back to regular node execution
        ReactiveNodeAction nodeAction = graph.getNodes().get(currentNode);
        if (nodeAction == null) {
            return Flux.error(new RuntimeException("Node not found: " + currentNode));
        }

        Mono<OverAllState> nodeExecution = nodeAction.apply(state)
                .map(updates -> {
                    state.updateState(updates);
                    return state;
                });

        return nodeExecution
                .flux()
                .concatWith(
                        nodeExecution
                                .flatMap(updatedState -> getNextNode(currentNode, updatedState))
                                .flatMapMany(nextNode -> streamNode(nextNode, state, iteration + 1))
                );
    }

    private Flux<OverAllState> streamEnhancedNode(String nodeId, EnhancedReactiveNodeAction action,
                                                 OverAllState state, int iteration) {
        // Create node input with current state and execution history
        Map<String, Object> directInputs = new HashMap<>(state.data());
        ReactiveNodeInput nodeInput = executionContext.createNodeInput(nodeId, directInputs);

        Mono<OverAllState> nodeExecution = action.applyEnhanced(state, nodeInput)
                .doOnNext(nodeOutput -> {
                    // Record the execution in context
                    executionContext.recordNodeExecution(nodeId, nodeInput, nodeOutput);
                })
                .map(nodeOutput -> {
                    // Apply state updates
                    state.updateState(nodeOutput.getStateUpdates());
                    return state;
                });

        return nodeExecution
                .flux()
                .concatWith(
                        nodeExecution
                                .flatMap(updatedState -> getNextNode(nodeId, updatedState))
                                .flatMapMany(nextNode -> streamNode(nextNode, state, iteration + 1))
                );
    }

    private Mono<String> getNextNode(String currentNode, OverAllState state) {
        // Check conditional edges first
        if (graph.getConditionalEdges().containsKey(currentNode)) {
            ReactiveEdgeAction edgeAction = graph.getEdgeActions().get(currentNode);
            Map<String, String> mapping = graph.getConditionalEdges().get(currentNode);
            
            return edgeAction.apply(state)
                    .map(result -> mapping.getOrDefault(result, ReactiveStateGraph.END));
        }
        
        // Check simple edges
        String nextNode = graph.getEdges().get(currentNode);
        return Mono.just(nextNode != null ? nextNode : ReactiveStateGraph.END);
    }
}
