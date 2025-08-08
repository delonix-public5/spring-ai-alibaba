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

/**
 * @author heguangfu
 * @date 2024/8/30 22:17
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatChoice implements Serializable {

    @Serial
    private static final long serialVersionUID = 4974000745799362378L;

    /**
     * The reason that this chat completions choice completed its generated.
     */
    private String finishReason;

    /**
     * The ordered index associated with this chat completions choice.
     */
    private Integer index;

    /**
     * The chat message for a given chat completions prompt.
     */
    private ChatMessage message;

    /**
     * The delta message content for a streaming response.
     */
    private ChatMessage delta;

//    /**
//     * Log probability information for the choice.
//     */
//    private ChatLogprobs logprobs;


}
