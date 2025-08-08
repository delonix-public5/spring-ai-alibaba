package com.alibaba.cloud.ai.graph.reactive.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * QwenProperties
 *
 * @author yangcanfeng
 * @version 2024/11/5
 */
@Data
@ConfigurationProperties(QwenProperties.CONFIG_PREFIX)
public class QwenProperties {

    public static final String CONFIG_PREFIX = "wormhole.ai.aliyun.qwen";

//    @JsonAlias(value = {"apikey"})
    private String apiKey="sk-c89068d244624ef8b8f0464404debeb3";

    private String endpoint="https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private String embeddingEndpoint;

}
