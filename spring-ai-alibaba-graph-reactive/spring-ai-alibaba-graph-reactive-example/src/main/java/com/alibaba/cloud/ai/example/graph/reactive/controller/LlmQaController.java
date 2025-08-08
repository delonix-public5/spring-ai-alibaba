package com.alibaba.cloud.ai.example.graph.reactive.controller;


import com.alibaba.cloud.ai.graph.reactive.ReactiveCompiledGraph;
import com.alibaba.cloud.ai.graph.reactive.ReactiveStateGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class LlmQaController {
    private static final Logger logger = LoggerFactory.getLogger(LlmQaController.class);


    private final ReactiveCompiledGraph compiledGraph;

    public LlmQaController(@Qualifier("reactiveLLMGraph") ReactiveStateGraph stateGraph) {
        this.compiledGraph = stateGraph.compile();
    }

    @GetMapping("/qa")
    public Mono<String> qa(@RequestParam(value = "input", defaultValue = "深圳天气怎么样") String input) {
       return compiledGraph.invoke(Map.of("input", input))
                .map(state -> state.value("output")
                        .map(Object::toString)
                        .orElse("天气不错"))
                .doOnSuccess(result -> logger.info("LlmQaController.qa(),result: {}", result))
                .doOnError(error -> logger.error("LlmQaController failed", error));
    }
}
