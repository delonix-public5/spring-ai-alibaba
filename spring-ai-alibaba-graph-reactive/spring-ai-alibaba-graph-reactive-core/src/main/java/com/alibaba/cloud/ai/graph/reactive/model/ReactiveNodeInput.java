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
package com.alibaba.cloud.ai.graph.reactive.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents input parameters for a reactive node execution.
 * Supports parameter passing between nodes with parameter name collision handling.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveNodeInput implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The current node's direct input parameters.
     */
    private final Map<String, Object> directInputs;

    /**
     * Historical inputs from all previous nodes, keyed by nodeId.parameterName.
     */
    private final Map<String, Map<String, Object>> nodeInputHistory;

    /**
     * Historical outputs from all previous nodes, keyed by nodeId.parameterName.
     */
    private final Map<String, Map<String, Object>> nodeOutputHistory;

    /**
     * The current node ID for context.
     */
    private final String currentNodeId;

    public ReactiveNodeInput(String currentNodeId) {
        this.currentNodeId = currentNodeId;
        this.directInputs = new HashMap<>();
        this.nodeInputHistory = new HashMap<>();
        this.nodeOutputHistory = new HashMap<>();
    }

    public ReactiveNodeInput(String currentNodeId, Map<String, Object> directInputs,
                           Map<String, Map<String, Object>> nodeInputHistory,
                           Map<String, Map<String, Object>> nodeOutputHistory) {
        this.currentNodeId = currentNodeId;
        this.directInputs = new HashMap<>(directInputs);
        this.nodeInputHistory = new HashMap<>(nodeInputHistory);
        this.nodeOutputHistory = new HashMap<>(nodeOutputHistory);
    }

    /**
     * Get direct input parameter value.
     *
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getDirectInput(String parameterName) {
        return Optional.ofNullable(directInputs.get(parameterName));
    }

    /**
     * Get input parameter from a specific node.
     *
     * @param nodeId the node ID
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getNodeInput(String nodeId, String parameterName) {
        return Optional.ofNullable(nodeInputHistory.get(nodeId))
                .map(inputs -> inputs.get(parameterName));
    }

    /**
     * Get output parameter from a specific node.
     *
     * @param nodeId the node ID
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getNodeOutput(String nodeId, String parameterName) {
        return Optional.ofNullable(nodeOutputHistory.get(nodeId))
                .map(outputs -> outputs.get(parameterName));
    }

    /**
     * Get all input parameters from a specific node.
     *
     * @param nodeId the node ID
     * @return all input parameters from the node
     */
    public Map<String, Object> getAllNodeInputs(String nodeId) {
        return Collections.unmodifiableMap(
                nodeInputHistory.getOrDefault(nodeId, Collections.emptyMap()));
    }

    /**
     * Get all output parameters from a specific node.
     *
     * @param nodeId the node ID
     * @return all output parameters from the node
     */
    public Map<String, Object> getAllNodeOutputs(String nodeId) {
        return Collections.unmodifiableMap(
                nodeOutputHistory.getOrDefault(nodeId, Collections.emptyMap()));
    }

    /**
     * Get the latest output parameter with the given name from any previous node.
     *
     * @param parameterName the parameter name
     * @return the latest parameter value
     */
    public Optional<Object> getLatestOutput(String parameterName) {
        return nodeOutputHistory.values().stream()
                .filter(outputs -> outputs.containsKey(parameterName))
                .reduce((first, second) -> second) // Get the last one
                .map(outputs -> outputs.get(parameterName));
    }

    /**
     * Get all direct input parameters.
     *
     * @return unmodifiable map of direct inputs
     */
    public Map<String, Object> getDirectInputs() {
        return Collections.unmodifiableMap(directInputs);
    }

    /**
     * Get all node input history.
     *
     * @return unmodifiable map of node input history
     */
    public Map<String, Map<String, Object>> getNodeInputHistory() {
        return Collections.unmodifiableMap(nodeInputHistory);
    }

    /**
     * Get all node output history.
     *
     * @return unmodifiable map of node output history
     */
    public Map<String, Map<String, Object>> getNodeOutputHistory() {
        return Collections.unmodifiableMap(nodeOutputHistory);
    }

    /**
     * Get current node ID.
     *
     * @return the current node ID
     */
    public String getCurrentNodeId() {
        return currentNodeId;
    }

    /**
     * Add direct input parameter.
     *
     * @param parameterName the parameter name
     * @param value the parameter value
     */
    public void addDirectInput(String parameterName, Object value) {
        directInputs.put(parameterName, value);
    }

    /**
     * Add node input history.
     *
     * @param nodeId the node ID
     * @param inputs the input parameters
     */
    public void addNodeInputHistory(String nodeId, Map<String, Object> inputs) {
        nodeInputHistory.put(nodeId, new HashMap<>(inputs));
    }

    /**
     * Add node output history.
     *
     * @param nodeId the node ID
     * @param outputs the output parameters
     */
    public void addNodeOutputHistory(String nodeId, Map<String, Object> outputs) {
        nodeOutputHistory.put(nodeId, new HashMap<>(outputs));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactiveNodeInput that = (ReactiveNodeInput) o;
        return Objects.equals(currentNodeId, that.currentNodeId) &&
                Objects.equals(directInputs, that.directInputs) &&
                Objects.equals(nodeInputHistory, that.nodeInputHistory) &&
                Objects.equals(nodeOutputHistory, that.nodeOutputHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentNodeId, directInputs, nodeInputHistory, nodeOutputHistory);
    }

    @Override
    public String toString() {
        return "ReactiveNodeInput{" +
                "currentNodeId='" + currentNodeId + '\'' +
                ", directInputs=" + directInputs +
                ", nodeInputHistory=" + nodeInputHistory +
                ", nodeOutputHistory=" + nodeOutputHistory +
                '}';
    }
}
