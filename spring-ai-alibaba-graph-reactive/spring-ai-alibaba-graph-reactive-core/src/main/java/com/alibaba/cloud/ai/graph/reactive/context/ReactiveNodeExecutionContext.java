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
package com.alibaba.cloud.ai.graph.reactive.context;

import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeInput;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeOutput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the execution context for reactive nodes, including input/output history
 * and dependency relationships between nodes.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveNodeExecutionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Execution order of nodes.
     */
    private final List<String> executionOrder;

    /**
     * Node input history, keyed by node ID.
     */
    private final Map<String, Map<String, Object>> nodeInputHistory;

    /**
     * Node output history, keyed by node ID.
     */
    private final Map<String, Map<String, Object>> nodeOutputHistory;

    /**
     * Node execution metadata, keyed by node ID.
     */
    private final Map<String, Map<String, Object>> nodeMetadata;

    /**
     * Current execution step.
     */
    private int currentStep;

    public ReactiveNodeExecutionContext() {
        this.executionOrder = new ArrayList<>();
        this.nodeInputHistory = new LinkedHashMap<>();
        this.nodeOutputHistory = new LinkedHashMap<>();
        this.nodeMetadata = new LinkedHashMap<>();
        this.currentStep = 0;
    }

    /**
     * Create node input for the given node ID.
     *
     * @param nodeId the node ID
     * @param directInputs the direct input parameters for this node
     * @return the node input
     */
    public ReactiveNodeInput createNodeInput(String nodeId, Map<String, Object> directInputs) {
        return new ReactiveNodeInput(nodeId, directInputs, 
                new HashMap<>(nodeInputHistory), new HashMap<>(nodeOutputHistory));
    }

    /**
     * Record node execution result.
     *
     * @param nodeId the node ID
     * @param nodeInput the node input that was used
     * @param nodeOutput the node output that was produced
     */
    public void recordNodeExecution(String nodeId, ReactiveNodeInput nodeInput, ReactiveNodeOutput nodeOutput) {
        // Record execution order
        if (!executionOrder.contains(nodeId)) {
            executionOrder.add(nodeId);
        }

        // Record input history
        nodeInputHistory.put(nodeId, new HashMap<>(nodeInput.getDirectInputs()));

        // Record output history
        nodeOutputHistory.put(nodeId, new HashMap<>(nodeOutput.getOutputs()));

        // Record metadata
        nodeMetadata.put(nodeId, new HashMap<>(nodeOutput.getAllMetadata()));

        currentStep++;
    }

    /**
     * Get node input history for a specific node.
     *
     * @param nodeId the node ID
     * @return the input history
     */
    public Optional<Map<String, Object>> getNodeInputHistory(String nodeId) {
        return Optional.ofNullable(nodeInputHistory.get(nodeId))
                .map(inputs -> Collections.unmodifiableMap(inputs));
    }

    /**
     * Get node output history for a specific node.
     *
     * @param nodeId the node ID
     * @return the output history
     */
    public Optional<Map<String, Object>> getNodeOutputHistory(String nodeId) {
        return Optional.ofNullable(nodeOutputHistory.get(nodeId))
                .map(outputs -> Collections.unmodifiableMap(outputs));
    }

    /**
     * Get node metadata for a specific node.
     *
     * @param nodeId the node ID
     * @return the metadata
     */
    public Optional<Map<String, Object>> getNodeMetadata(String nodeId) {
        return Optional.ofNullable(nodeMetadata.get(nodeId))
                .map(metadata -> Collections.unmodifiableMap(metadata));
    }

    /**
     * Get all executed node IDs in execution order.
     *
     * @return the execution order
     */
    public List<String> getExecutionOrder() {
        return Collections.unmodifiableList(executionOrder);
    }

    /**
     * Get all node input history.
     *
     * @return the complete input history
     */
    public Map<String, Map<String, Object>> getAllNodeInputHistory() {
        return Collections.unmodifiableMap(nodeInputHistory);
    }

    /**
     * Get all node output history.
     *
     * @return the complete output history
     */
    public Map<String, Map<String, Object>> getAllNodeOutputHistory() {
        return Collections.unmodifiableMap(nodeOutputHistory);
    }

    /**
     * Get all node metadata.
     *
     * @return the complete metadata
     */
    public Map<String, Map<String, Object>> getAllNodeMetadata() {
        return Collections.unmodifiableMap(nodeMetadata);
    }

    /**
     * Get current execution step.
     *
     * @return the current step
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Check if a node has been executed.
     *
     * @param nodeId the node ID
     * @return true if the node has been executed
     */
    public boolean hasNodeBeenExecuted(String nodeId) {
        return executionOrder.contains(nodeId);
    }

    /**
     * Get the latest output parameter with the given name from any executed node.
     *
     * @param parameterName the parameter name
     * @return the latest parameter value and the node ID that produced it
     */
    public Optional<NodeParameterResult> getLatestOutput(String parameterName) {
        for (int i = executionOrder.size() - 1; i >= 0; i--) {
            String nodeId = executionOrder.get(i);
            Map<String, Object> outputs = nodeOutputHistory.get(nodeId);
            if (outputs != null && outputs.containsKey(parameterName)) {
                return Optional.of(new NodeParameterResult(nodeId, parameterName, outputs.get(parameterName)));
            }
        }
        return Optional.empty();
    }

    /**
     * Get all output parameters with the given name from all executed nodes.
     *
     * @param parameterName the parameter name
     * @return list of parameter results
     */
    public List<NodeParameterResult> getAllOutputs(String parameterName) {
        List<NodeParameterResult> results = new ArrayList<>();
        for (String nodeId : executionOrder) {
            Map<String, Object> outputs = nodeOutputHistory.get(nodeId);
            if (outputs != null && outputs.containsKey(parameterName)) {
                results.add(new NodeParameterResult(nodeId, parameterName, outputs.get(parameterName)));
            }
        }
        return results;
    }

    /**
     * Clear all execution history.
     */
    public void clear() {
        executionOrder.clear();
        nodeInputHistory.clear();
        nodeOutputHistory.clear();
        nodeMetadata.clear();
        currentStep = 0;
    }

    /**
     * Create a snapshot of the current execution context.
     *
     * @return a new execution context with the same state
     */
    public ReactiveNodeExecutionContext snapshot() {
        ReactiveNodeExecutionContext snapshot = new ReactiveNodeExecutionContext();
        snapshot.executionOrder.addAll(this.executionOrder);
        this.nodeInputHistory.forEach((nodeId, inputs) -> 
                snapshot.nodeInputHistory.put(nodeId, new HashMap<>(inputs)));
        this.nodeOutputHistory.forEach((nodeId, outputs) -> 
                snapshot.nodeOutputHistory.put(nodeId, new HashMap<>(outputs)));
        this.nodeMetadata.forEach((nodeId, metadata) -> 
                snapshot.nodeMetadata.put(nodeId, new HashMap<>(metadata)));
        snapshot.currentStep = this.currentStep;
        return snapshot;
    }

    /**
     * Represents a parameter result from a specific node.
     */
    public static class NodeParameterResult {
        private final String nodeId;
        private final String parameterName;
        private final Object value;

        public NodeParameterResult(String nodeId, String parameterName, Object value) {
            this.nodeId = nodeId;
            this.parameterName = parameterName;
            this.value = value;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getParameterName() {
            return parameterName;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "NodeParameterResult{" +
                    "nodeId='" + nodeId + '\'' +
                    ", parameterName='" + parameterName + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
