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
package com.alibaba.cloud.ai.graph.reactive.util;

import com.alibaba.cloud.ai.graph.reactive.context.ReactiveNodeExecutionContext;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeInput;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for resolving and accessing node input/output parameters.
 * Provides convenient APIs for querying previous nodes' inputs and outputs.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class NodeIOResolver {

    private final ReactiveNodeInput nodeInput;
    private final ReactiveNodeExecutionContext executionContext;

    public NodeIOResolver(ReactiveNodeInput nodeInput, ReactiveNodeExecutionContext executionContext) {
        this.nodeInput = nodeInput;
        this.executionContext = executionContext;
    }

    /**
     * Get input parameter from the current node.
     *
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getInput(String parameterName) {
        return nodeInput.getDirectInput(parameterName);
    }

    /**
     * Get input parameter from a specific node.
     *
     * @param nodeId the node ID
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getInput(String nodeId, String parameterName) {
        return nodeInput.getNodeInput(nodeId, parameterName);
    }

    /**
     * Get output parameter from a specific node.
     *
     * @param nodeId the node ID
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getOutput(String nodeId, String parameterName) {
        return nodeInput.getNodeOutput(nodeId, parameterName);
    }

    /**
     * Get the latest output parameter with the given name from any previous node.
     *
     * @param parameterName the parameter name
     * @return the latest parameter value
     */
    public Optional<Object> getLatestOutput(String parameterName) {
        return nodeInput.getLatestOutput(parameterName);
    }

    /**
     * Get the latest output parameter with the given name and the node that produced it.
     *
     * @param parameterName the parameter name
     * @return the parameter result with node information
     */
    public Optional<ReactiveNodeExecutionContext.NodeParameterResult> getLatestOutputWithNode(String parameterName) {
        return executionContext.getLatestOutput(parameterName);
    }

    /**
     * Get all output parameters with the given name from all previous nodes.
     *
     * @param parameterName the parameter name
     * @return list of parameter results
     */
    public List<ReactiveNodeExecutionContext.NodeParameterResult> getAllOutputs(String parameterName) {
        return executionContext.getAllOutputs(parameterName);
    }

    /**
     * Get all input parameters from a specific node.
     *
     * @param nodeId the node ID
     * @return all input parameters from the node
     */
    public Map<String, Object> getAllInputs(String nodeId) {
        return nodeInput.getAllNodeInputs(nodeId);
    }

    /**
     * Get all output parameters from a specific node.
     *
     * @param nodeId the node ID
     * @return all output parameters from the node
     */
    public Map<String, Object> getAllOutputs(String nodeId) {
        return nodeInput.getAllNodeOutputs(nodeId);
    }

    /**
     * Get all executed node IDs in execution order.
     *
     * @return the execution order
     */
    public List<String> getExecutionOrder() {
        return executionContext.getExecutionOrder();
    }

    /**
     * Check if a node has been executed.
     *
     * @param nodeId the node ID
     * @return true if the node has been executed
     */
    public boolean hasNodeBeenExecuted(String nodeId) {
        return executionContext.hasNodeBeenExecuted(nodeId);
    }

    /**
     * Get the current node ID.
     *
     * @return the current node ID
     */
    public String getCurrentNodeId() {
        return nodeInput.getCurrentNodeId();
    }

    /**
     * Get all direct input parameters for the current node.
     *
     * @return all direct input parameters
     */
    public Map<String, Object> getDirectInputs() {
        return nodeInput.getDirectInputs();
    }

    /**
     * Find nodes that produced a specific output parameter.
     *
     * @param parameterName the parameter name
     * @return list of node IDs that produced this parameter
     */
    public List<String> findNodesWithOutput(String parameterName) {
        return getAllOutputs(parameterName).stream()
                .map(ReactiveNodeExecutionContext.NodeParameterResult::getNodeId)
                .collect(Collectors.toList());
    }

    /**
     * Find nodes that received a specific input parameter.
     *
     * @param parameterName the parameter name
     * @return list of node IDs that received this parameter
     */
    public List<String> findNodesWithInput(String parameterName) {
        return nodeInput.getNodeInputHistory().entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(parameterName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get typed input parameter from the current node.
     *
     * @param parameterName the parameter name
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getInput(String parameterName, Class<T> type) {
        return getInput(parameterName)
                .filter(type::isInstance)
                .map(value -> (T) value);
    }

    /**
     * Get typed output parameter from a specific node.
     *
     * @param nodeId the node ID
     * @param parameterName the parameter name
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOutput(String nodeId, String parameterName, Class<T> type) {
        return getOutput(nodeId, parameterName)
                .filter(type::isInstance)
                .map(value -> (T) value);
    }

    /**
     * Get typed latest output parameter.
     *
     * @param parameterName the parameter name
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getLatestOutput(String parameterName, Class<T> type) {
        return getLatestOutput(parameterName)
                .filter(type::isInstance)
                .map(value -> (T) value);
    }

    /**
     * Create a resolver from node input and execution context.
     *
     * @param nodeInput the node input
     * @param executionContext the execution context
     * @return a new resolver
     */
    public static NodeIOResolver of(ReactiveNodeInput nodeInput, ReactiveNodeExecutionContext executionContext) {
        return new NodeIOResolver(nodeInput, executionContext);
    }
}
