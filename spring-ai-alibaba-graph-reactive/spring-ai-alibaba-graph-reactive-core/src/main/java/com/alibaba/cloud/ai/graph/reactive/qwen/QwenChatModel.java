//package com.alibaba.cloud.ai.graph.reactive.qwen;
//
//import com.wormhole.agent.ai.aliyun.config.QwenProperties;
//import com.wormhole.agent.core.chat.ChatModel;
//import com.wormhole.agent.model.openai.OpenAiChatParams;
//import com.wormhole.common.util.JacksonUtils;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//
///**
// * QwenChatModel
// *
// * @author yangcanfeng
// * @version 2024/11/5
// */
//@Slf4j
//@Component
//public class QwenChatModel implements ChatModel {
//
//    private static final WebClient WEB_CLIENT = WebClient.builder()
//            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .build();
//    @Resource
//    private QwenProperties qwenProperties;
//
//    @Override
//    public Flux<String> chatCompletions(OpenAiChatParams chatClientParams) {
//        chatClientParams.setEnableThinking(false);
//        log.info(JacksonUtils.writeValuePretty(chatClientParams));
//        return WEB_CLIENT.post()
//                .uri(qwenProperties.getEndpoint())
//                .header(HttpHeaders.AUTHORIZATION, qwenProperties.getApiKey())
//                .body(Mono.just(JacksonUtils.writeValueAsString(chatClientParams)), String.class)
//                .retrieve()
//                .bodyToFlux(String.class)
//                .timeout(Duration.ofMinutes(2L))
//                .doOnError(e -> {
//                    log.error(e.getMessage(), e);
//                });
//    }
//
//
//}
