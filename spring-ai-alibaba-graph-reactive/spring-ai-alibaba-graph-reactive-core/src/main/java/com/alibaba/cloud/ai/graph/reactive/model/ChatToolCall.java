package com.alibaba.cloud.ai.graph.reactive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatToolCall implements Serializable {

    @Serial
    private static final long serialVersionUID = -7240152627074353986L;

    private Integer index;

    /**
     * The ID of the tool call.
     */
    private String id;

    /**
     * The type of the tool. Currently, only function is supported.
     */
    private String type;

    /**
     * The function that the model called.
     */
    private ChatFunctionCall function;
}
