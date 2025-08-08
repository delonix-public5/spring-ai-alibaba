package com.alibaba.cloud.ai.graph.reactive.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author heguangfu
 * @date 2024/9/2 17:10
 **/
@Getter
public enum ChatFinishReason {

    STOP("stop"),
    LENGTH("length"),
    CONTENT_FILTER("content_filter"),
    FUNCTION_CALL("function_call"),
    TOOL_CALLS("tool_calls"),
    ;

    private final String reason;

    ChatFinishReason(String reason) {
        this.reason = reason;
    }

    public static boolean isFinishReason(String finishReason) {
        return StringUtils.equalsAnyIgnoreCase(finishReason,
                ChatFinishReason.STOP.getReason(),
                ChatFinishReason.LENGTH.getReason(),
                ChatFinishReason.CONTENT_FILTER.toString(),
                ChatFinishReason.FUNCTION_CALL.toString(),
                ChatFinishReason.TOOL_CALLS.toString());
    }

    public static ChatFinishReason from(String value) {
        return Optional.ofNullable(value)
                .filter(StringUtils::isNotBlank)
                .flatMap(t -> Arrays.stream(values()).filter(item -> item.getReason().equalsIgnoreCase(value)).findFirst())
                .orElse(null);
    }

}
