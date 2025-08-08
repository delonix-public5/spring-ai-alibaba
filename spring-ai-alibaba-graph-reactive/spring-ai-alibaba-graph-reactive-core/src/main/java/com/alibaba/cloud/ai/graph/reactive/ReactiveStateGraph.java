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

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveEdgeAction;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveNodeAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Reactive version of StateGraph.
 * Provides a reactive workflow execution engine using WebFlux.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveStateGraph {

    public static final String START = "__START__";
    public static final String END = "__END__";

    private final String name;
    private final Supplier<Map<String, KeyStrategy>> stateFactory;
    private final Map<String, ReactiveNodeAction> nodes = new ConcurrentHashMap<>();
    private final Map<String, String> edges = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> conditionalEdges = new ConcurrentHashMap<>();
    private final Map<String, ReactiveEdgeAction> edgeActions = new ConcurrentHashMap<>();

    public ReactiveStateGraph(String name, Supplier<Map<String, KeyStrategy>> stateFactory) {
        this.name = name;
        this.stateFactory = stateFactory;
    }

    /**
     * Add a node to the graph.
     *
     * @param nodeName the name of the node
     * @param action the reactive action to execute
     * @return this graph for method chaining
     */
    public ReactiveStateGraph addNode(String nodeName, ReactiveNodeAction action) {
        nodes.put(nodeName, action);
        return this;
    }

    /**
     * Add a simple edge between two nodes.
     *
     * @param fromNode the source node
     * @param toNode the target node
     * @return this graph for method chaining
     */
    public ReactiveStateGraph addEdge(String fromNode, String toNode) {
        edges.put(fromNode, toNode);
        return this;
    }

    /**
     * Add conditional edges from a node.
     *
     * @param fromNode the source node
     * @param edgeAction the action to determine the next node
     * @param mapping the mapping from edge action result to target nodes
     * @return this graph for method chaining
     */
    public ReactiveStateGraph addConditionalEdges(String fromNode, ReactiveEdgeAction edgeAction, 
                                                  Map<String, String> mapping) {
        conditionalEdges.put(fromNode, new HashMap<>(mapping));
        edgeActions.put(fromNode, edgeAction);
        return this;
    }

    /**
     * Compile the graph into an executable form.
     *
     * @return a compiled reactive graph
     */
    public ReactiveCompiledGraph compile() {
        return new ReactiveCompiledGraph(this);
    }

    // Getters for internal access
    public String getName() {
        return name;
    }

    public Supplier<Map<String, KeyStrategy>> getStateFactory() {
        return stateFactory;
    }

    public Map<String, ReactiveNodeAction> getNodes() {
        return nodes;
    }

    public Map<String, String> getEdges() {
        return edges;
    }

    public Map<String, Map<String, String>> getConditionalEdges() {
        return conditionalEdges;
    }

    public Map<String, ReactiveEdgeAction> getEdgeActions() {
        return edgeActions;
    }
}
