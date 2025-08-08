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
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveNodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Reactive version of RecordingNode.
 * Records and processes customer feedback results reactively.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveRecordingNode implements ReactiveNodeAction {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveRecordingNode.class);

    @Override
    public Mono<Map<String, Object>> apply(OverAllState state) {
        return Mono.fromCallable(() -> {
            String feedback = (String) state.value("classifier_output").orElse("");
            
            Map<String, Object> updatedState = new HashMap<>();
            
            if (feedback.contains("positive")) {
                logger.info("Reactive - Received positive feedback: {}", feedback);
                updatedState.put("solution", "Praise, no action taken.");
            } else {
                logger.info("Reactive - Received negative feedback: {}", feedback);
                updatedState.put("solution", feedback);
            }
            
            return updatedState;
        })
        .doOnSuccess(result -> logger.debug("Reactive recording completed: {}", result))
        .doOnError(error -> logger.error("Reactive recording failed", error));
    }
}
