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

/**
 * Reactive version of EdgeAction interface.
 * Defines a reactive edge action that can be executed asynchronously
 * and returns a Mono containing the next node name.
 *
 * @author Your Name
 * @since 1.0.0
 */
@FunctionalInterface
public interface ReactiveEdgeAction {

    /**
     * Apply the edge action reactively to determine the next node.
     *
     * @param state the current overall state
     * @return a Mono containing the name of the next node
     */
    Mono<String> apply(OverAllState state);

    /**
     * Create a reactive edge action from a synchronous one.
     *
     * @param syncAction the synchronous edge action
     * @return a reactive edge action
     */
    static ReactiveEdgeAction fromSync(com.alibaba.cloud.ai.graph.action.EdgeAction syncAction) {
        return state -> Mono.fromCallable(() -> {
            try {
                return syncAction.apply(state);
            } catch (Exception e) {
                throw new RuntimeException("Error executing synchronous edge action", e);
            }
        });
    }

    /**
     * Create a reactive edge action that always returns the same node name.
     *
     * @param nodeName the node name to return
     * @return a reactive edge action
     */
    static ReactiveEdgeAction constant(String nodeName) {
        return state -> Mono.just(nodeName);
    }
}
