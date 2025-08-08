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
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reactive version of NodeAction interface.
 * Defines a reactive node action that can be executed asynchronously
 * and returns a Mono containing the updated state.
 *
 * @author Your Name
 * @since 1.0.0
 */
@FunctionalInterface
public interface ReactiveNodeAction {

    /**
     * Apply the node action reactively.
     *
     * @param state the current overall state
     * @return a Mono containing the updated state map
     */
    Mono<Map<String, Object>> apply(OverAllState state);

    /**
     * Create a reactive node action from a synchronous one.
     *
     * @param syncAction the synchronous node action
     * @return a reactive node action
     */
    static ReactiveNodeAction fromSync(com.alibaba.cloud.ai.graph.action.NodeAction syncAction) {
        return state -> Mono.fromCallable(() -> {
            try {
                return syncAction.apply(state);
            } catch (Exception e) {
                throw new RuntimeException("Error executing synchronous node action", e);
            }
        });
    }

    /**
     * Create a reactive node action that returns empty state.
     *
     * @return a reactive node action that returns empty map
     */
    static ReactiveNodeAction empty() {
        return state -> Mono.just(Map.of());
    }
}
