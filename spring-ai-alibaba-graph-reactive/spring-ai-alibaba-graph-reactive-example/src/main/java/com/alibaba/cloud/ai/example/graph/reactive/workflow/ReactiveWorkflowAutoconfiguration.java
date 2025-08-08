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
import com.alibaba.cloud.ai.graph.reactive.ReactiveStateGraph;
import com.alibaba.cloud.ai.graph.reactive.config.QwenProperties;
import com.alibaba.cloud.ai.graph.reactive.node.ReactiveLlmNode;
import com.alibaba.cloud.ai.graph.reactive.node.ReactiveQuestionClassifierNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactive version of WorkflowAutoconfiguration.
 * Configures reactive workflow graph for customer service.
 *
 * @author Your Name
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(QwenProperties.class)
public class ReactiveWorkflowAutoconfiguration {

    @Bean
    public ReactiveStateGraph reactiveWorkflowGraph(ChatModel chatModel) {
        // Create reactive ChatClient
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        // Create reactive feedback classifier
        ReactiveQuestionClassifierNode feedbackClassifier = ReactiveQuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .outputKey("classifier_output")
                .categories(List.of("positive feedback", "negative feedback"))
                .classificationInstructions(
                        List.of("Try to understand the user's feeling when he/she is giving the feedback."))
                .build();

        // Create reactive specific question classifier
        ReactiveQuestionClassifierNode specificQuestionClassifier = ReactiveQuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .outputKey("classifier_output")
                .categories(List.of("after-sale service", "transportation", "product quality", "others"))
                .classificationInstructions(List.of(
                        "What kind of service or help the customer is trying to get from us? " +
                        "Classify the question based on your understanding."))
                .build();

        // Create reactive recording node
        ReactiveRecordingNode recordingNode = new ReactiveRecordingNode();

        // Build reactive state graph
        return new ReactiveStateGraph(
                "Reactive Consumer Service Workflow Demo", 
                () -> {
                    Map<String, KeyStrategy> strategies = new HashMap<>();
                    strategies.put("input", new ReplaceStrategy());
                    strategies.put("classifier_output", new ReplaceStrategy());
                    strategies.put("solution", new ReplaceStrategy());
                    return strategies;
                })
                .addNode("feedback_classifier", feedbackClassifier)
                .addNode("specific_question_classifier", specificQuestionClassifier)
                .addNode("recorder", recordingNode)

                // Define edges
                .addEdge(ReactiveStateGraph.START, "feedback_classifier")
                .addConditionalEdges("feedback_classifier",
                        new ReactiveCustomerServiceController.ReactiveFeedbackQuestionDispatcher(),
                        Map.of("positive", "recorder", "negative", "specific_question_classifier"))
                .addConditionalEdges("specific_question_classifier",
                        new ReactiveCustomerServiceController.ReactiveSpecificQuestionDispatcher(),
                        Map.of("after-sale", "recorder", "transportation", "recorder", 
                               "quality", "recorder", "others", "recorder"))
                .addEdge("recorder", ReactiveStateGraph.END);
    }

//    @Bean
//    public ReactiveStateGraph reactiveLLMGraph(QwenProperties qwenProperties) {
//
//        // Build reactive state graph
//        return new ReactiveStateGraph(
//                "Reactive Consumer Service Workflow Demo",
//                () -> {
//                    Map<String, KeyStrategy> strategies = new HashMap<>();
//                    strategies.put("input", new ReplaceStrategy());
//                    strategies.put("output", new ReplaceStrategy());
//                    return strategies;
//                })
//                .addNode("llm", new ReactiveLlmNode(qwenProperties, "Please answer the following question: {input}"))
//
//                // Define edges
//                .addEdge(ReactiveStateGraph.START, "llm")
//                .addEdge("llm", ReactiveStateGraph.END);
//    }
@Bean
public ReactiveStateGraph reactiveLLMGraph(ChatModel chatModel) {
    // Create reactive ChatClient
    ChatClient chatClient = ChatClient.builder(chatModel)
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();

    // Build reactive state graph
    ReactiveStateGraph stateGraph = new ReactiveStateGraph(
            "Reactive Consumer Service Workflow Demo",
            () -> {
                Map<String, KeyStrategy> strategies = new HashMap<>();
                strategies.put("input", new ReplaceStrategy());
                strategies.put("output", new ReplaceStrategy());
                return strategies;
            })
            .addNode("llm", new ReactiveLlmNode(chatClient, "Please answer the following question: {input}"))

            // Define edges
            .addEdge(ReactiveStateGraph.START, "llm")
            .addEdge("llm", ReactiveStateGraph.END);

    return stateGraph;
}
}
