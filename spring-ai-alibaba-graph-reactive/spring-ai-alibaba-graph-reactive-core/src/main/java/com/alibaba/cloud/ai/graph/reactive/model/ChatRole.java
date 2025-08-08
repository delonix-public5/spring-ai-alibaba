package com.alibaba.cloud.ai.graph.reactive.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * ChatRole
 *
 * @author yangcanfeng
 * @version 2024/11/27
 */
@Getter
public enum ChatRole {

    /**
     * developer
     */
    DEVELOPER("developer"),
    /**
     * system
     */
    SYSTEM("system"),
    /**
     * user
     */
    USER("user"),
    /**
     * assistant
     */
    ASSISTANT("assistant"),
    /**
     * function
     */
    FUNCTION("function"),
    /**
     * tool
     */
    TOOL("tool");

    private final String value;

    ChatRole(final String value) {
        this.value = value;
    }

    public static ChatRole from(String value) {
        return Optional.ofNullable(value)
                .filter(StringUtils::isNotBlank)
                .flatMap(t -> Arrays.stream(values()).filter(item -> item.getValue().equalsIgnoreCase(value)).findFirst())
                .orElse(null);
    }

}
