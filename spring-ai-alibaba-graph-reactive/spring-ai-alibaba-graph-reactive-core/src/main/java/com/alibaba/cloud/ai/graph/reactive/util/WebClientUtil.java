package com.alibaba.cloud.ai.graph.reactive.util;

import com.alibaba.cloud.ai.graph.reactive.config.QwenProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientUtil {

    public static ReactiveChatClient createReactiveChatClient(QwenProperties qwenProperties) {
        WebClient.Builder webClientBuilder = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new ReactiveChatClient(webClientBuilder, qwenProperties);
    }

}