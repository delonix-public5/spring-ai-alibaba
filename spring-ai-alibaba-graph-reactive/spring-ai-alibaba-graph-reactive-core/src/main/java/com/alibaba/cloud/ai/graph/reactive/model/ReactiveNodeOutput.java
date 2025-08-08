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
 * Represents output parameters from a reactive node execution.
 * Supports parameter passing between nodes with parameter name collision handling.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveNodeOutput implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The node ID that produced this output.
     */
    private final String nodeId;

    /**
     * The output parameters produced by this node.
     */
    private final Map<String, Object> outputs;

    /**
     * The global state updates to be applied.
     */
    private final Map<String, Object> stateUpdates;

    /**
     * Metadata about the node execution.
     */
    private final Map<String, Object> metadata;

    public ReactiveNodeOutput(String nodeId) {
        this.nodeId = nodeId;
        this.outputs = new HashMap<>();
        this.stateUpdates = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public ReactiveNodeOutput(String nodeId, Map<String, Object> outputs) {
        this.nodeId = nodeId;
        this.outputs = new HashMap<>(outputs);
        this.stateUpdates = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public ReactiveNodeOutput(String nodeId, Map<String, Object> outputs, 
                            Map<String, Object> stateUpdates) {
        this.nodeId = nodeId;
        this.outputs = new HashMap<>(outputs);
        this.stateUpdates = new HashMap<>(stateUpdates);
        this.metadata = new HashMap<>();
    }

    public ReactiveNodeOutput(String nodeId, Map<String, Object> outputs, 
                            Map<String, Object> stateUpdates, Map<String, Object> metadata) {
        this.nodeId = nodeId;
        this.outputs = new HashMap<>(outputs);
        this.stateUpdates = new HashMap<>(stateUpdates);
        this.metadata = new HashMap<>(metadata);
    }

    /**
     * Get output parameter value.
     *
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public Optional<Object> getOutput(String parameterName) {
        return Optional.ofNullable(outputs.get(parameterName));
    }

    /**
     * Get state update value.
     *
     * @param key the state key
     * @return the state value
     */
    public Optional<Object> getStateUpdate(String key) {
        return Optional.ofNullable(stateUpdates.get(key));
    }

    /**
     * Get metadata value.
     *
     * @param key the metadata key
     * @return the metadata value
     */
    public Optional<Object> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    /**
     * Get all output parameters.
     *
     * @return unmodifiable map of outputs
     */
    public Map<String, Object> getOutputs() {
        return Collections.unmodifiableMap(outputs);
    }

    /**
     * Get all state updates.
     *
     * @return unmodifiable map of state updates
     */
    public Map<String, Object> getStateUpdates() {
        return Collections.unmodifiableMap(stateUpdates);
    }

    /**
     * Get all metadata.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, Object> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Get the node ID.
     *
     * @return the node ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Add output parameter.
     *
     * @param parameterName the parameter name
     * @param value the parameter value
     * @return this output for method chaining
     */
    public ReactiveNodeOutput addOutput(String parameterName, Object value) {
        outputs.put(parameterName, value);
        return this;
    }

    /**
     * Add multiple output parameters.
     *
     * @param outputs the output parameters
     * @return this output for method chaining
     */
    public ReactiveNodeOutput addOutputs(Map<String, Object> outputs) {
        this.outputs.putAll(outputs);
        return this;
    }

    /**
     * Add state update.
     *
     * @param key the state key
     * @param value the state value
     * @return this output for method chaining
     */
    public ReactiveNodeOutput addStateUpdate(String key, Object value) {
        stateUpdates.put(key, value);
        return this;
    }

    /**
     * Add multiple state updates.
     *
     * @param stateUpdates the state updates
     * @return this output for method chaining
     */
    public ReactiveNodeOutput addStateUpdates(Map<String, Object> stateUpdates) {
        this.stateUpdates.putAll(stateUpdates);
        return this;
    }

    /**
     * Add metadata.
     *
     * @param key the metadata key
     * @param value the metadata value
     * @return this output for method chaining
     */
    public ReactiveNodeOutput addMetadata(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    /**
     * Add multiple metadata entries.
     *
     * @param metadata the metadata
     * @return this output for method chaining
     */
    public ReactiveNodeOutput addMetadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }

    /**
     * Create a builder for ReactiveNodeOutput.
     *
     * @param nodeId the node ID
     * @return a new builder
     */
    public static Builder builder(String nodeId) {
        return new Builder(nodeId);
    }

    /**
     * Builder for ReactiveNodeOutput.
     */
    public static class Builder {
        private final String nodeId;
        private final Map<String, Object> outputs = new HashMap<>();
        private final Map<String, Object> stateUpdates = new HashMap<>();
        private final Map<String, Object> metadata = new HashMap<>();

        public Builder(String nodeId) {
            this.nodeId = nodeId;
        }

        public Builder output(String name, Object value) {
            outputs.put(name, value);
            return this;
        }

        public Builder outputs(Map<String, Object> outputs) {
            this.outputs.putAll(outputs);
            return this;
        }

        public Builder stateUpdate(String key, Object value) {
            stateUpdates.put(key, value);
            return this;
        }

        public Builder stateUpdates(Map<String, Object> stateUpdates) {
            this.stateUpdates.putAll(stateUpdates);
            return this;
        }

        public Builder metadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public ReactiveNodeOutput build() {
            return new ReactiveNodeOutput(nodeId, outputs, stateUpdates, metadata);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactiveNodeOutput that = (ReactiveNodeOutput) o;
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(outputs, that.outputs) &&
                Objects.equals(stateUpdates, that.stateUpdates) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, outputs, stateUpdates, metadata);
    }

    @Override
    public String toString() {
        return "ReactiveNodeOutput{" +
                "nodeId='" + nodeId + '\'' +
                ", outputs=" + outputs +
                ", stateUpdates=" + stateUpdates +
                ", metadata=" + metadata +
                '}';
    }
}
