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
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.action.EnhancedReactiveNodeAction;
import com.alibaba.cloud.ai.graph.reactive.context.ReactiveNodeExecutionContext;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeInput;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeOutput;
import com.alibaba.cloud.ai.graph.reactive.util.NodeIOResolver;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for enhanced node I/O functionality.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class EnhancedNodeIOTest {

    @Test
    public void testBasicNodeIO() {
        // Create a simple graph with enhanced nodes
        ReactiveStateGraph graph = new ReactiveStateGraph(
                "Test Graph",
                () -> {
                    Map<String, KeyStrategy> strategies = new HashMap<>();
                    strategies.put("input", new ReplaceStrategy());
                    strategies.put("result", new ReplaceStrategy());
                    return strategies;
                })
                .addEnhancedNode("node1", new TestNode1())
                .addEnhancedNode("node2", new TestNode2())
                .addEdge(ReactiveStateGraph.START, "node1")
                .addEdge("node1", "node2")
                .addEdge("node2", ReactiveStateGraph.END);

        ReactiveCompiledGraph compiledGraph = graph.compile();

        // Test execution
        StepVerifier.create(compiledGraph.invoke(Map.of("input", "test")))
                .assertNext(state -> {
                    assertEquals("test_processed_transformed", state.value("result").orElse(""));
                    
                    // Check execution context
                    ReactiveNodeExecutionContext context = compiledGraph.getExecutionContext();
                    assertEquals(2, context.getCurrentStep());
                    assertTrue(context.hasNodeBeenExecuted("node1"));
                    assertTrue(context.hasNodeBeenExecuted("node2"));
                    
                    // Check node outputs
                    assertTrue(context.getNodeOutputHistory("node1").isPresent());
                    assertTrue(context.getNodeOutputHistory("node2").isPresent());
                })
                .verifyComplete();
    }

    @Test
    public void testParameterNameCollision() {
        // Test that nodes can have parameters with the same name
        ReactiveStateGraph graph = new ReactiveStateGraph(
                "Collision Test Graph",
                () -> {
                    Map<String, KeyStrategy> strategies = new HashMap<>();
                    strategies.put("input", new ReplaceStrategy());
                    return strategies;
                })
                .addEnhancedNode("nodeA", new CollisionNodeA())
                .addEnhancedNode("nodeB", new CollisionNodeB())
                .addEnhancedNode("nodeC", new CollisionNodeC())
                .addEdge(ReactiveStateGraph.START, "nodeA")
                .addEdge("nodeA", "nodeB")
                .addEdge("nodeB", "nodeC")
                .addEdge("nodeC", ReactiveStateGraph.END);

        ReactiveCompiledGraph compiledGraph = graph.compile();

        StepVerifier.create(compiledGraph.invoke(Map.of("input", "test")))
                .assertNext(state -> {
                    ReactiveNodeExecutionContext context = compiledGraph.getExecutionContext();
                    
                    // Check that each node has its own "data" parameter
                    assertEquals("test_A", context.getNodeOutputHistory("nodeA")
                            .map(outputs -> outputs.get("data")).orElse(""));
                    assertEquals("test_A_B", context.getNodeOutputHistory("nodeB")
                            .map(outputs -> outputs.get("data")).orElse(""));
                    assertEquals("test_A_B_C", context.getNodeOutputHistory("nodeC")
                            .map(outputs -> outputs.get("data")).orElse(""));
                })
                .verifyComplete();
    }

    @Test
    public void testNodeIOResolver() {
        ReactiveNodeInput nodeInput = new ReactiveNodeInput("testNode");
        nodeInput.addDirectInput("param1", "value1");
        nodeInput.addNodeOutputHistory("prevNode", Map.of("output1", "prevValue"));

        ReactiveNodeExecutionContext context = new ReactiveNodeExecutionContext();
        NodeIOResolver resolver = NodeIOResolver.of(nodeInput, context);

        // Test direct input access
        assertEquals("value1", resolver.getInput("param1").orElse(""));
        assertEquals("value1", resolver.getInput("param1", String.class).orElse(""));

        // Test previous node output access
        assertEquals("prevValue", resolver.getOutput("prevNode", "output1").orElse(""));
        assertEquals("prevValue", resolver.getOutput("prevNode", "output1", String.class).orElse(""));

        // Test current node ID
        assertEquals("testNode", resolver.getCurrentNodeId());
    }

    // Test node implementations
    public static class TestNode1 implements EnhancedReactiveNodeAction {
        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                String input = resolver.getInput("input", String.class).orElse("");
                
                return ReactiveNodeOutput.builder("node1")
                        .output("processed", input + "_processed")
                        .stateUpdate("intermediate", input + "_processed")
                        .build();
            });
        }
    }

    public static class TestNode2 implements EnhancedReactiveNodeAction {
        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                String processed = resolver.getOutput("node1", "processed", String.class).orElse("");
                
                return ReactiveNodeOutput.builder("node2")
                        .output("transformed", processed + "_transformed")
                        .stateUpdate("result", processed + "_transformed")
                        .build();
            });
        }
    }

    // Nodes for testing parameter name collision
    public static class CollisionNodeA implements EnhancedReactiveNodeAction {
        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                String input = resolver.getInput("input", String.class).orElse("");
                
                return ReactiveNodeOutput.builder("nodeA")
                        .output("data", input + "_A")
                        .build();
            });
        }
    }

    public static class CollisionNodeB implements EnhancedReactiveNodeAction {
        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                String dataFromA = resolver.getOutput("nodeA", "data", String.class).orElse("");
                
                return ReactiveNodeOutput.builder("nodeB")
                        .output("data", dataFromA + "_B")
                        .build();
            });
        }
    }

    public static class CollisionNodeC implements EnhancedReactiveNodeAction {
        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                String dataFromB = resolver.getOutput("nodeB", "data", String.class).orElse("");
                
                return ReactiveNodeOutput.builder("nodeC")
                        .output("data", dataFromB + "_C")
                        .build();
            });
        }
    }
}
