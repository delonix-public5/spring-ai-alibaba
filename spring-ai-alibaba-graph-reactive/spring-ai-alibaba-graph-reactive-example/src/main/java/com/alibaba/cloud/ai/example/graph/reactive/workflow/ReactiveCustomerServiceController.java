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
package com.alibaba.cloud.ai.example.graph.reactive.workflow;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.ReactiveCompiledGraph;
import com.alibaba.cloud.ai.graph.reactive.ReactiveStateGraph;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveEdgeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Reactive version of CustomerServiceController using WebFlux.
 * Handles customer service workflow requests reactively.
 *
 * @author Your Name
 * @since 1.0.0
 */
@RestController
@RequestMapping("/reactive/customer")
public class ReactiveCustomerServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveCustomerServiceController.class);

    private final ReactiveCompiledGraph compiledGraph;

    public ReactiveCustomerServiceController(@Qualifier("reactiveWorkflowGraph") ReactiveStateGraph stateGraph) {
        this.compiledGraph = stateGraph.compile();
    }

    /**
     * Handle customer service chat requests reactively.
     *
     * @param query the customer query
     * @return a Mono containing the response
     */
    @GetMapping("/chat")
    public Mono<String> simpleChat(@RequestParam String query) {
        logger.info("Received reactive customer service request: {}", query);
        
        return compiledGraph.invoke(Map.of("input", query))
                .map(state -> state.value("solution")
                        .map(Object::toString)
                        .orElse("No solution found"))
                .doOnSuccess(result -> logger.info("Reactive chat completed with result: {}", result))
                .doOnError(error -> logger.error("Reactive chat failed", error));
    }

    /**
     * Stream customer service chat requests, returning intermediate states.
     *
     * @param query the customer query
     * @return a Flux of intermediate states
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String query) {
        logger.info("Received reactive streaming customer service request: {}", query);
        
        return compiledGraph.stream(Map.of("input", query))
                .map(state -> {
                    String solution = state.value("solution")
                            .map(Object::toString)
                            .orElse("");
                    String classifierOutput = state.value("classifier_output")
                            .map(Object::toString)
                            .orElse("");
                    
                    return String.format("State: classifier=%s, solution=%s", classifierOutput, solution);
                })
                .doOnNext(state -> logger.debug("Streaming state: {}", state))
                .doOnComplete(() -> logger.info("Reactive streaming completed"))
                .doOnError(error -> logger.error("Reactive streaming failed", error));
    }

    /**
     * Reactive version of FeedbackQuestionDispatcher.
     */
    public static class ReactiveFeedbackQuestionDispatcher implements ReactiveEdgeAction {

        @Override
        public Mono<String> apply(OverAllState state) {
            return Mono.fromCallable(() -> {
                String classifierOutput = (String) state.value("classifier_output").orElse("");
                logger.info("Reactive feedback dispatcher - classifierOutput: {}", classifierOutput);

                if (classifierOutput.contains("positive")) {
                    return "positive";
                }
                return "negative";
            });
        }
    }

    /**
     * Reactive version of SpecificQuestionDispatcher.
     */
    public static class ReactiveSpecificQuestionDispatcher implements ReactiveEdgeAction {

        @Override
        public Mono<String> apply(OverAllState state) {
            return Mono.fromCallable(() -> {
                String classifierOutput = (String) state.value("classifier_output").orElse("");
                logger.info("Reactive specific question dispatcher - classifierOutput: {}", classifierOutput);

                Map<String, String> classifierMap = new HashMap<>();
                classifierMap.put("after-sale", "after-sale");
                classifierMap.put("quality", "quality");
                classifierMap.put("transportation", "transportation");

                for (Map.Entry<String, String> entry : classifierMap.entrySet()) {
                    if (classifierOutput.contains(entry.getKey())) {
                        return entry.getValue();
                    }
                }

                return "others";
            });
        }
    }
}
