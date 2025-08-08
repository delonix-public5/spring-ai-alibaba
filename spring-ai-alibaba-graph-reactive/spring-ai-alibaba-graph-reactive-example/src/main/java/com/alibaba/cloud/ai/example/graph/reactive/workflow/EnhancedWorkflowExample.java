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

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.ReactiveCompiledGraph;
import com.alibaba.cloud.ai.graph.reactive.ReactiveStateGraph;
import com.alibaba.cloud.ai.graph.reactive.action.EnhancedReactiveNodeAction;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeInput;
import com.alibaba.cloud.ai.graph.reactive.model.ReactiveNodeOutput;
import com.alibaba.cloud.ai.graph.reactive.util.NodeIOResolver;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating the enhanced node I/O mechanism.
 * Shows how nodes can access inputs and outputs from previous nodes.
 *
 * @author Your Name
 * @since 1.0.0
 */
@Component
public class EnhancedWorkflowExample {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedWorkflowExample.class);

    /**
     * Create an enhanced workflow graph that demonstrates node I/O capabilities.
     *
     * @return the compiled graph
     */
    public ReactiveCompiledGraph createEnhancedWorkflow() {
        ReactiveStateGraph graph = new ReactiveStateGraph(
                "Enhanced Workflow Demo",
                () -> {
                    Map<String, KeyStrategy> strategies = new HashMap<>();
                    strategies.put("input", new ReplaceStrategy());
                    strategies.put("result", new ReplaceStrategy());
                    return strategies;
                })
                .addEnhancedNode("data_processor", new DataProcessorNode())
                .addEnhancedNode("data_transformer", new DataTransformerNode())
                .addEnhancedNode("data_aggregator", new DataAggregatorNode())
                .addEnhancedNode("result_formatter", new ResultFormatterNode())

                // Define edges
                .addEdge(ReactiveStateGraph.START, "data_processor")
                .addEdge("data_processor", "data_transformer")
                .addEdge("data_transformer", "data_aggregator")
                .addEdge("data_aggregator", "result_formatter")
                .addEdge("result_formatter", ReactiveStateGraph.END);

        return graph.compile();
    }

    /**
     * First node: processes input data and produces multiple outputs.
     */
    public static class DataProcessorNode implements EnhancedReactiveNodeAction {

        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                
                // Get input data
                String input = resolver.getInput("input", String.class).orElse("");
                logger.info("DataProcessor processing input: {}", input);

                // Process data and create multiple outputs
                return ReactiveNodeOutput.builder("data_processor")
                        .output("processed_text", input.toUpperCase())
                        .output("word_count", input.split("\\s+").length)
                        .output("char_count", input.length())
                        .stateUpdate("processing_step", "data_processed")
                        .metadata("processing_time", System.currentTimeMillis())
                        .build();
            });
        }
    }

    /**
     * Second node: transforms data using outputs from the first node.
     */
    public static class DataTransformerNode implements EnhancedReactiveNodeAction {

        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                
                // Get outputs from previous node
                String processedText = resolver.getOutput("data_processor", "processed_text")
                        .map(Object::toString).orElse("");
                Integer wordCount = resolver.getOutput("data_processor", "word_count", Integer.class).orElse(0);
                
                logger.info("DataTransformer received: text={}, wordCount={}", processedText, wordCount);

                // Transform data
                String transformedText = processedText.replace(" ", "_");
                double avgWordLength = processedText.length() / (double) Math.max(wordCount, 1);

                return ReactiveNodeOutput.builder("data_transformer")
                        .output("transformed_text", transformedText)
                        .output("avg_word_length", avgWordLength)
                        .output("transformation_type", "underscore_replacement")
                        .stateUpdate("processing_step", "data_transformed")
                        .build();
            });
        }
    }

    /**
     * Third node: aggregates data from multiple previous nodes.
     */
    public static class DataAggregatorNode implements EnhancedReactiveNodeAction {

        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                
                // Get data from multiple previous nodes
                Integer wordCount = resolver.getOutput("data_processor", "word_count", Integer.class).orElse(0);
                Integer charCount = resolver.getOutput("data_processor", "char_count", Integer.class).orElse(0);
                Double avgWordLength = resolver.getOutput("data_transformer", "avg_word_length", Double.class).orElse(0.0);
                String transformedText = resolver.getOutput("data_transformer", "transformed_text")
                        .map(Object::toString).orElse("");

                logger.info("DataAggregator aggregating: wordCount={}, charCount={}, avgWordLength={}", 
                           wordCount, charCount, avgWordLength);

                // Create aggregated data
                Map<String, Object> aggregatedData = new HashMap<>();
                aggregatedData.put("total_words", wordCount);
                aggregatedData.put("total_chars", charCount);
                aggregatedData.put("avg_word_length", avgWordLength);
                aggregatedData.put("processed_text", transformedText);

                return ReactiveNodeOutput.builder("data_aggregator")
                        .output("aggregated_data", aggregatedData)
                        .output("summary", String.format("Processed %d words, %d chars, avg length %.2f", 
                                                        wordCount, charCount, avgWordLength))
                        .stateUpdate("processing_step", "data_aggregated")
                        .build();
            });
        }
    }

    /**
     * Fourth node: formats final result using all previous node outputs.
     */
    public static class ResultFormatterNode implements EnhancedReactiveNodeAction {

        @Override
        public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
            return Mono.fromCallable(() -> {
                NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
                
                // Get original input
                String originalInput = resolver.getInput("input", String.class).orElse("");
                
                // Get summary from aggregator
                String summary = resolver.getOutput("data_aggregator", "summary")
                        .map(Object::toString).orElse("");
                
                // Get all execution order to show the workflow
                logger.info("ResultFormatter creating final result");

                // Create final formatted result
                StringBuilder result = new StringBuilder();
                result.append("=== Workflow Processing Result ===\n");
                result.append("Original Input: ").append(originalInput).append("\n");
                result.append("Processing Summary: ").append(summary).append("\n");
                result.append("Processing Steps:\n");
                
                // Show data from each step
                resolver.getOutput("data_processor", "processed_text")
                        .ifPresent(text -> result.append("  1. Processed: ").append(text).append("\n"));
                resolver.getOutput("data_transformer", "transformed_text")
                        .ifPresent(text -> result.append("  2. Transformed: ").append(text).append("\n"));
                resolver.getOutput("data_aggregator", "aggregated_data")
                        .ifPresent(data -> result.append("  3. Aggregated: ").append(data).append("\n"));

                return ReactiveNodeOutput.builder("result_formatter")
                        .output("final_result", result.toString())
                        .stateUpdate("result", result.toString())
                        .stateUpdate("processing_step", "completed")
                        .metadata("completion_time", System.currentTimeMillis())
                        .build();
            });
        }
    }

    /**
     * Run the enhanced workflow example.
     *
     * @param input the input text to process
     * @return the final result
     */
    public Mono<String> runExample(String input) {
        ReactiveCompiledGraph graph = createEnhancedWorkflow();
        
        return graph.invoke(Map.of("input", input))
                .map(state -> state.value("result")
                        .map(Object::toString)
                        .orElse("No result found"))
                .doOnSuccess(result -> logger.info("Enhanced workflow completed successfully"))
                .doOnError(error -> logger.error("Enhanced workflow failed", error));
    }
}
