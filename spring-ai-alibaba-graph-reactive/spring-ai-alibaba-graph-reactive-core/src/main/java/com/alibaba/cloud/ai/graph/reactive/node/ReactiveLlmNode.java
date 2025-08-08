package com.alibaba.cloud.ai.graph.reactive.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.reactive.action.ReactiveNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

public class ReactiveLlmNode implements ReactiveNodeAction {

    private final ChatClient chatClient;

    private final String promptTemplate;

    public ReactiveLlmNode(ChatClient chatClient, String promptTemplate) {
        this.chatClient = chatClient;
        this.promptTemplate = promptTemplate;
    }

    @Override
    public Mono<Map<String, Object>> apply(OverAllState state) {
        return Mono.fromCallable(() -> (String) state.value("input").orElse(""))
                .filter(input -> !input.isEmpty())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Input text is empty")))
                .flatMap(this::classifyText)
                .map(result -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("output", result);
                    return updates;
                })
                .onErrorReturn(Map.of("outputKey", "unknown"));
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
        promptBuilder.append("你是一个全能的ai助手");
        promptBuilder.append(String.join(", ", inputText));
        promptBuilder.append("\n\n");

        return promptBuilder.toString();
    }
}
