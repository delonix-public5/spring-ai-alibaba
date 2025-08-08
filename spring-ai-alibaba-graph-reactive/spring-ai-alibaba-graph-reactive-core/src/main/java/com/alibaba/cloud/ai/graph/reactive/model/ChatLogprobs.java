package com.alibaba.cloud.ai.graph.reactive.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author heguangfu
 * @date 2024/8/30 22:17
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatLogprobs implements Serializable {

    @Serial
    private static final long serialVersionUID = 4974000745799362378L;

    private String finishReason;

    private List<ChatLogprobDetail> content;

}
