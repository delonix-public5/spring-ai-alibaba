package com.alibaba.cloud.ai.graph.reactive.util;

import com.alibaba.cloud.ai.graph.reactive.config.QwenProperties;
import com.alibaba.cloud.ai.graph.reactive.model.ChatCompletions;
import com.alibaba.cloud.ai.graph.reactive.model.OpenAiChatParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class ReactiveChatClient {

    private final WebClient webClient;
    private final QwenProperties qwenProperties;
    private final ObjectMapper objectMapper;

    public ReactiveChatClient(WebClient.Builder webClientBuilder, QwenProperties qwenProperties) {
        this.webClient = webClientBuilder.build();
        this.qwenProperties = qwenProperties;
        this.objectMapper = new ObjectMapper();
    }

    public Flux<ChatCompletions> chat(OpenAiChatParams openAiChatParams) {
        return webClient.post()
                .uri(qwenProperties.getEndpoint())
                .header(HttpHeaders.AUTHORIZATION, qwenProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(openAiChatParams)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5).trim())
                .filter(data -> !"[DONE]".equals(data))
                .map(data -> {
                    try {
                        return objectMapper.readValue(data, ChatCompletions.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse ChatCompletions from stream data: {}", data, e);
                        throw new RuntimeException(e);
                    }
                })
                .timeout(Duration.ofMinutes(2L))
                .doOnError(e -> {
                    log.error("Error during chat stream processing", e);
                });
    }

}
