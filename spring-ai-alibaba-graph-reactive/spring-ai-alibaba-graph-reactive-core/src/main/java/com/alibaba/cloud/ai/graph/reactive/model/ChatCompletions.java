package com.alibaba.cloud.ai.graph.reactive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author heguangfu
 * @date 2024/8/30 22:17
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatCompletions implements Serializable {

    @Serial
    private static final long serialVersionUID = -4973722180655887679L;
    /**
     * A unique identifier associated with this chat completions response.
     */
    private String id;
    /**
     * The collection of completions choices associated with this completions response.
     * Generally, `n` choices are generated per provided prompt with a default value of 1.
     * Token limits and other settings may limit the number of choices generated.
     */
    private List<ChatChoice> choices;
    /**
     * The first timestamp associated with generation activity for this completions response,
     * represented as seconds since the beginning of the Unix epoch of 00:00 on 1 Jan 1970.
     */
    private Integer created;
    /**
     * The model used for the chat completion.
     */
    private String model;
    /**
     * The service tier used for processing the request. This field is only included if the service_tier parameter is specified in the request.
     */
    private String serviceTier;
    /**
     * only chunk
     * This fingerprint represents the backend configuration that the model runs with. Can be used in conjunction with the seed request parameter to understand when backend changes have been made that might impact determinism.
     */
    private String systemFingerprint;
    /**
     * The object type, which is always chat.completion.
     */
    private String object;
    /**
     * Usage information for tokens processed and generated as part of this completions operation.
     */
    private ChatUsage usage;
    /**
     * 分片序号，从0开始
     */
    private Integer chatSeq;
    /**
     * 是否结束
     */
    private Boolean chatEnd;
    /**
     * 模型异常
     */
    private Error error;
    /**
     * 元数据
     */
    @Builder.Default
    private Metadata metadata = Metadata.builder().build();
    /**
     * 事件
     */
    private String event;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Metadata {
        /**
         * traceId
         */
        private String traceId;
        /**
         * 会话id
         */
        private String conversationId;
        /**
         * 客户端clientReqId
         */
        private String clientReqId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Error {
        /**
         * A machine-readable error code.
         */
        private String code;
        /**
         * invalid_request_error
         */
        private String type;
        /**
         * A human-readable error message.
         */
        private String message;
        /**
         * 额外参数
         */
        private String param;
    }

    public boolean isChatEnd() {
        return Objects.nonNull(chatEnd) && chatEnd;
    }

}
