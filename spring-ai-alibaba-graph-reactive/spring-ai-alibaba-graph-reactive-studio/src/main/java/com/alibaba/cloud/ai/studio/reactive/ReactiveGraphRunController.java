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
package com.alibaba.cloud.ai.studio.reactive;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.ReactiveCompiledGraph;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reactive version of GraphRunController for studio management interface.
 * Provides reactive endpoints for graph execution and monitoring.
 *
 * @author Your Name
 * @since 1.0.0
 */
@RestController
@RequestMapping("/reactive/run")
public class ReactiveGraphRunController {

    private final ReactiveCompiledGraph graph;

    public ReactiveGraphRunController(@Qualifier("buildReactiveGraph") ReactiveCompiledGraph graph) {
        this.graph = graph;
    }

    /**
     * Stream graph execution results as Server-Sent Events.
     *
     * @param inputs the input parameters
     * @return a Flux of execution states
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ReactiveNodeOutput> stream(@RequestBody Map<String, Object> inputs) {
        return graph.stream(inputs)
                .map(state -> new ReactiveNodeOutput(
                        state.value("current_node").map(Object::toString).orElse("unknown"),
                        state.data(),
                        System.currentTimeMillis()
                ))
                .onErrorResume(error -> Flux.just(new ReactiveNodeOutput(
                        "error",
                        Map.of("error", error.getMessage()),
                        System.currentTimeMillis()
                )));
    }

    /**
     * Invoke graph execution and return final result.
     *
     * @param inputs the input parameters
     * @return a Mono containing the final state
     */
    @PostMapping(value = "/invoke")
    public Mono<OverAllState> invoke(@RequestBody Map<String, Object> inputs) {
        return graph.invoke(inputs)
                .onErrorResume(error -> Mono.just(createErrorState(error)));
    }

    /**
     * Get graph execution status.
     *
     * @return a Mono containing the status information
     */
    @GetMapping("/status")
    public Mono<Map<String, Object>> getStatus() {
        return Mono.just(Map.of(
                "status", "running",
                "type", "reactive",
                "timestamp", System.currentTimeMillis()
        ));
    }

    private OverAllState createErrorState(Throwable error) {
        OverAllState errorState = new OverAllState();
        Map<String, Object> errorData = Map.of(
            "error", error.getMessage(),
            "status", "failed"
        );
        errorState.updateState(errorData);
        return errorState;
    }

    /**
     * Reactive version of NodeOutput for streaming responses.
     */
    public static class ReactiveNodeOutput {
        private final String nodeName;
        private final Map<String, Object> state;
        private final long timestamp;

        public ReactiveNodeOutput(String nodeName, Map<String, Object> state, long timestamp) {
            this.nodeName = nodeName;
            this.state = state;
            this.timestamp = timestamp;
        }

        public String getNodeName() {
            return nodeName;
        }

        public Map<String, Object> getState() {
            return state;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
