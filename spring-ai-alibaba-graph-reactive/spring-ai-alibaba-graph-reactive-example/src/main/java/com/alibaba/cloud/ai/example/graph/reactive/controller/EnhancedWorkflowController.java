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
package com.alibaba.cloud.ai.example.graph.reactive.controller;

import com.alibaba.cloud.ai.example.graph.reactive.workflow.EnhancedWorkflowExample;
import com.alibaba.cloud.ai.graph.reactive.ReactiveCompiledGraph;
import com.alibaba.cloud.ai.graph.reactive.context.ReactiveNodeExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller for testing the enhanced workflow with node I/O capabilities.
 *
 * @author Your Name
 * @since 1.0.0
 */
@RestController
@RequestMapping("/reactive/enhanced")
public class EnhancedWorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedWorkflowController.class);

    @Autowired
    private EnhancedWorkflowExample enhancedWorkflowExample;

    /**
     * Test the enhanced workflow with node I/O.
     *
     * @param input the input text to process
     * @return the processed result
     */
    @GetMapping("/workflow")
    public Mono<String> testEnhancedWorkflow(@RequestParam(defaultValue = "Hello World from Enhanced Workflow") String input) {
        logger.info("Testing enhanced workflow with input: {}", input);
        
        return enhancedWorkflowExample.runExample(input)
                .doOnSuccess(result -> logger.info("Enhanced workflow test completed"))
                .doOnError(error -> logger.error("Enhanced workflow test failed", error));
    }

    /**
     * Stream the enhanced workflow execution, showing intermediate states.
     *
     * @param input the input text to process
     * @return a stream of intermediate states
     */
    @GetMapping(value = "/workflow/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEnhancedWorkflow(@RequestParam(defaultValue = "Hello World Stream") String input) {
        logger.info("Streaming enhanced workflow with input: {}", input);
        
        ReactiveCompiledGraph graph = enhancedWorkflowExample.createEnhancedWorkflow();
        
        return graph.stream(Map.of("input", input))
                .map(state -> {
                    String step = state.value("processing_step")
                            .map(Object::toString)
                            .orElse("unknown");
                    String result = state.value("result")
                            .map(Object::toString)
                            .orElse("");
                    
                    return String.format("Step: %s, Result: %s", step, 
                            result.length() > 100 ? result.substring(0, 100) + "..." : result);
                })
                .doOnNext(state -> logger.debug("Streaming state: {}", state))
                .doOnComplete(() -> logger.info("Enhanced workflow streaming completed"))
                .doOnError(error -> logger.error("Enhanced workflow streaming failed", error));
    }

    /**
     * Get execution context information after running the workflow.
     *
     * @param input the input text to process
     * @return execution context details
     */
    @GetMapping("/workflow/context")
    public Mono<Map<String, Object>> getExecutionContext(@RequestParam(defaultValue = "Context Test") String input) {
        logger.info("Getting execution context for input: {}", input);
        
        ReactiveCompiledGraph graph = enhancedWorkflowExample.createEnhancedWorkflow();
        
        return graph.invoke(Map.of("input", input))
                .map(state -> {
                    ReactiveNodeExecutionContext context = graph.getExecutionContext();
                    
                    return Map.of(
                            "executionOrder", context.getExecutionOrder(),
                            "currentStep", context.getCurrentStep(),
                            "nodeInputHistory", context.getAllNodeInputHistory(),
                            "nodeOutputHistory", context.getAllNodeOutputHistory(),
                            "nodeMetadata", context.getAllNodeMetadata()
                    );
                })
                .doOnSuccess(context -> logger.info("Execution context retrieved successfully"))
                .doOnError(error -> logger.error("Failed to get execution context", error));
    }

    /**
     * Test parameter name collision handling.
     *
     * @param input the input text
     * @return result showing how parameter name collisions are handled
     */
    @GetMapping("/workflow/collision-test")
    public Mono<String> testParameterCollision(@RequestParam(defaultValue = "Collision Test") String input) {
        logger.info("Testing parameter name collision with input: {}", input);
        
        // This would demonstrate how the same parameter name from different nodes is handled
        ReactiveCompiledGraph graph = enhancedWorkflowExample.createEnhancedWorkflow();
        
        return graph.invoke(Map.of("input", input))
                .map(state -> {
                    ReactiveNodeExecutionContext context = graph.getExecutionContext();
                    StringBuilder result = new StringBuilder();
                    
                    result.append("=== Parameter Collision Test ===\n");
                    result.append("Execution Order: ").append(context.getExecutionOrder()).append("\n\n");
                    
                    // Show how different nodes can have parameters with same names
                    context.getExecutionOrder().forEach(nodeId -> {
                        result.append("Node: ").append(nodeId).append("\n");
                        context.getNodeOutputHistory(nodeId).ifPresent(outputs -> {
                            result.append("  Outputs: ").append(outputs.keySet()).append("\n");
                        });
                    });
                    
                    return result.toString();
                })
                .doOnSuccess(result -> logger.info("Parameter collision test completed"))
                .doOnError(error -> logger.error("Parameter collision test failed", error));
    }
}
