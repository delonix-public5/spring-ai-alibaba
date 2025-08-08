//package com.alibaba.cloud.ai.graph.reactive.qwen;
//
//
//import com.jayway.jsonpath.JsonPath;
//import com.wormhole.agent.ai.aliyun.config.QwenProperties;
//import com.wormhole.agent.ai.core.auth.BearerUtils;
//import com.wormhole.agent.ai.model.UnifiedModelEnum;
//import com.wormhole.agent.core.embedding.EmbeddingClient;
//import com.wormhole.agent.core.embedding.model.EmbeddingParams;
//import com.wormhole.agent.core.model.ModelProviderEnum;
//import com.wormhole.agent.core.segment.model.Chunk;
//import com.wormhole.common.util.JacksonUtils;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.ConnectTimeoutException;
//import io.netty.handler.timeout.ReadTimeoutHandler;
//import io.netty.handler.timeout.WriteTimeoutHandler;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.MapUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.netty.http.client.HttpClient;
//import reactor.netty.resources.ConnectionProvider;
//import reactor.util.retry.Retry;
//
//import java.net.SocketTimeoutException;
//import java.time.Duration;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding-api-details
// *
// * @author heguangfu
// * @date 2024/9/9 11:34
// **/
//@Slf4j
//@Service
//public class QwenEmbeddingClient implements EmbeddingClient {
//
//    private static final WebClient WEB_CLIENT;
//
////    static {
////        WEB_CLIENT = WebClient.builder()
////                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
////                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 设置为 5MB
////                .build();
////    }
//
//    static {
//        ConnectionProvider connectionProvider = ConnectionProvider.builder("qwen-embedding-pool")
//                .maxConnections(100)
//                .pendingAcquireMaxCount(500)
//                .pendingAcquireTimeout(Duration.ofSeconds(5))
//                .lifo()
//                .build();
//        HttpClient httpClient = HttpClient.create(connectionProvider)
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
//                .responseTimeout(Duration.ofSeconds(30))
//                .doOnConnected(conn -> conn
//                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
//                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
//                )
//                .keepAlive(true);
//        WEB_CLIENT = WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
//                .build();
//    }
//
//    @Resource
//    private QwenProperties qwenProperties;
//
//    @Override
//    public boolean support(String model) {
//        UnifiedModelEnum unifiedModelEnum = UnifiedModelEnum.findByModel(model);
//        return ModelProviderEnum.QWEN.getProvider().equalsIgnoreCase(Optional.ofNullable(unifiedModelEnum).map(UnifiedModelEnum::getProvider).orElse(null));
//    }
//
//    @Override
//    public Mono<List<Float>> create(EmbeddingParams embeddingParams) {
//        return doRequest(embeddingParams)
//                .map(result -> {
//                    if (StringUtils.isBlank(result)) {
//                        return Collections.emptyList();
//                    }
//                    // 配置 JsonPath 使用 Jackson 提供程序
//                    List<Number> list = JsonPath.read(result, "$.output.embeddings[0].embedding");
//                    return list.stream().map(item -> item.floatValue()).collect(Collectors.toUnmodifiableList());
//                });
//    }
//
//    @Override
//    public Mono<List<List<Float>>> batchCreate(EmbeddingParams embeddingParams) {
//        return doRequest(embeddingParams)
//                .map(result -> {
//                    if (StringUtils.isBlank(result)) {
//                        return Collections.emptyList();
//                    }
//                    List<Map<String, Object>> list = JsonPath.read(result, "$.output.embeddings");
//                    return list.stream()
//                            .map(itemMap -> {
//                                List<Float> embedding = (List<Float>) MapUtils.getObject(itemMap, "embedding");
//                                return embedding;
//                            })
//                            .collect(Collectors.toUnmodifiableList());
//                });
//    }
//
////    @Override
////    public Mono<List<Chunk>> batchCreateForObj(EmbeddingParams embeddingParams) {
////        return doRequest(embeddingParams)
////                .map(result -> {
////                    if (StringUtils.isBlank(result)) {
////                        return Collections.emptyList();
////                    }
////                    List<Map<String, Object>> list = JsonPath.read(result, "$.output.embeddings");
////                    return list.stream()
////                            .map(itemMap -> {
////                                Integer index = MapUtils.getInteger(itemMap, "text_index");
////                                List<Number> numberList = (List<Number>) MapUtils.getObject(itemMap, "embedding");
////                                List<Float> embedding = numberList.stream().map(number -> number.floatValue()).toList();
////                                return Chunk.builder().index(index).embedding(embedding).build();
////                            })
////                            .collect(Collectors.toUnmodifiableList());
////                });
////    }
//
//    @Override
//    public Mono<List<Chunk>> batchCreateForObj(EmbeddingParams embeddingParams) {
//        return doRequestConcurrent(embeddingParams)
//                .flatMapMany(Flux::fromIterable)
//                .filter(StringUtils::isNotBlank)
//                .map(result -> {
//                    List<String> inputs = getInputList(embeddingParams.getInput());
//                    List<Map<String, Object>> list = JsonPath.read(result, "$.output.embeddings");
//                    return list.stream()
//                            .map(itemMap -> {
//                                Integer index = MapUtils.getInteger(itemMap, "text_index");
//                                List<Number> numberList = (List<Number>) MapUtils.getObject(itemMap, "embedding");
//                                List<Float> embedding = numberList.stream().map(Number::floatValue).toList();
//                                return Chunk.builder().index(index).embedding(embedding).content(inputs.get(index))
//                                        .build();
//                            })
//                            .collect(Collectors.toUnmodifiableList());
//                })
//                .collectList()
//                .map(list -> list.stream().flatMap(Collection::stream).toList()); // 把多个批次结果展平
//    }
//
//
//
//    public Mono<String> doRequest(EmbeddingParams embeddingParams) {
//        List<String> inputs = getInputList(embeddingParams.getInput());
//        String body = JacksonUtils.writeValueAsString(Map.of(
//                "model", embeddingParams.getModel(),
//                "input", Map.of("texts", inputs)
//        ));
//        return WEB_CLIENT.post()
//                .uri(qwenProperties.getEmbeddingEndpoint())
//                .header(HttpHeaders.AUTHORIZATION, BearerUtils.getBearerToken(qwenProperties.getApiKey()))
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(String.class);
//    }
//
//    public Mono<List<String>> doRequestConcurrent(EmbeddingParams embeddingParams) {
//        List<String> inputs = getInputList(embeddingParams.getInput());
//        return Flux.fromIterable(inputs)
//                .buffer(20)
//                .delayElements(Duration.ofMillis(500))
//                .flatMap(batch -> {
//                    String body = JacksonUtils.writeValueAsString(Map.of(
//                            "model", embeddingParams.getModel(),
//                            "input", Map.of("texts", batch)
//                    ));
//
//                    return WEB_CLIENT.post()
//                            .uri(qwenProperties.getEmbeddingEndpoint())
//                            .header(HttpHeaders.AUTHORIZATION, BearerUtils.getBearerToken(qwenProperties.getApiKey()))
//                            .bodyValue(body)
//                            .retrieve()
//                            .bodyToMono(String.class)
//                            .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
//                                    .maxBackoff(Duration.ofSeconds(3))
//                                    .jitter(0.2)
//                                    .filter(throwable -> throwable instanceof SocketTimeoutException || throwable instanceof ConnectTimeoutException)
//                                    .doBeforeRetry(retrySignal ->
//                                            log.error("Retry attempt {} due to: {}", retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())
//                                    )
//                            )
//                            .doOnError(throwable ->
//                                    log.error("Request failed after retries, error_msg: {}", throwable.getMessage(), throwable)
//                            );
//                }, 1)
//                .collectList();
//    }
//
//
//
//
//}
