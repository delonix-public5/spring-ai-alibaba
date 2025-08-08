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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public ReactiveCompiledGraph(ReactiveStateGraph graph) {
        this.graph = graph;
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

        ReactiveNodeAction nodeAction = graph.getNodes().get(currentNode);
        if (nodeAction == null && !ReactiveStateGraph.START.equals(currentNode)) {
            return Mono.error(new RuntimeException("Node not found: " + currentNode));
        }

        Mono<OverAllState> nodeExecution;
        if (ReactiveStateGraph.START.equals(currentNode)) {
            nodeExecution = Mono.just(state);
        } else {
            nodeExecution = nodeAction.apply(state)
                    .map(updates -> {
                        state.updateState(updates);
                        return state;
                    });
        }

        return nodeExecution
                .flatMap(updatedState -> getNextNode(currentNode, updatedState))
                .flatMap(nextNode -> executeNode(nextNode, state, iteration + 1));
    }

    private Flux<OverAllState> streamNode(String currentNode, OverAllState state, int iteration) {
        if (iteration >= maxIterations) {
            return Flux.error(new RuntimeException("Maximum iterations exceeded: " + maxIterations));
        }

        if (ReactiveStateGraph.END.equals(currentNode)) {
            return Flux.just(state);
        }

        ReactiveNodeAction nodeAction = graph.getNodes().get(currentNode);
        if (nodeAction == null && !ReactiveStateGraph.START.equals(currentNode)) {
            return Flux.error(new RuntimeException("Node not found: " + currentNode));
        }

        Mono<OverAllState> nodeExecution;
        if (ReactiveStateGraph.START.equals(currentNode)) {
            nodeExecution = Mono.just(state);
        } else {
            nodeExecution = nodeAction.apply(state)
                    .map(updates -> {
                        state.updateState(updates);
                        return state;
                    });
        }

        return nodeExecution
                .flux()
                .concatWith(
                        nodeExecution
                                .flatMap(updatedState -> getNextNode(currentNode, updatedState))
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
