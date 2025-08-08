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
package com.alibaba.cloud.ai.graph.reactive.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactive version of QuestionClassifierNode.
 * Classifies questions using AI models in a reactive manner.
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ReactiveQuestionClassifierNode implements ReactiveNodeAction {

    private final ChatClient chatClient;
    private final String inputTextKey;
    private final String outputKey;
    private final List<String> categories;
    private final List<String> classificationInstructions;

    private ReactiveQuestionClassifierNode(Builder builder) {
        this.chatClient = builder.chatClient;
        this.inputTextKey = builder.inputTextKey;
        this.outputKey = builder.outputKey;
        this.categories = builder.categories;
        this.classificationInstructions = builder.classificationInstructions;
    }

    @Override
    public Mono<Map<String, Object>> apply(OverAllState state) {
        return Mono.fromCallable(() -> (String) state.value(inputTextKey).orElse(""))
                .filter(input -> !input.isEmpty())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Input text is empty")))
                .flatMap(this::classifyText)
                .map(result -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(outputKey, result);
                    return updates;
                })
                .onErrorReturn(Map.of(outputKey, "unknown"));
    }

    private Mono<String> classifyText(String inputText) {
        return Mono.fromCallable(() -> {
            String prompt = buildPrompt(inputText);
            return chatClient.prompt(prompt).call().content();
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    private String buildPrompt(String inputText) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Please classify the following text into one of these categories: ");
        promptBuilder.append(String.join(", ", categories));
        promptBuilder.append("\n\n");
        
        if (classificationInstructions != null && !classificationInstructions.isEmpty()) {
            promptBuilder.append("Instructions:\n");
            for (String instruction : classificationInstructions) {
                promptBuilder.append("- ").append(instruction).append("\n");
            }
            promptBuilder.append("\n");
        }
        
        promptBuilder.append("Text to classify: ").append(inputText);
        promptBuilder.append("\n\nPlease respond with only the category name.");
        
        return promptBuilder.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChatClient chatClient;
        private String inputTextKey = "input";
        private String outputKey = "classifier_output";
        private List<String> categories;
        private List<String> classificationInstructions;

        public Builder chatClient(ChatClient chatClient) {
            this.chatClient = chatClient;
            return this;
        }

        public Builder inputTextKey(String inputTextKey) {
            this.inputTextKey = inputTextKey;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public Builder classificationInstructions(List<String> instructions) {
            this.classificationInstructions = instructions;
            return this;
        }

        public ReactiveQuestionClassifierNode build() {
            if (chatClient == null) {
                throw new IllegalArgumentException("ChatClient is required");
            }
            if (categories == null || categories.isEmpty()) {
                throw new IllegalArgumentException("Categories are required");
            }
            return new ReactiveQuestionClassifierNode(this);
        }
    }
}
