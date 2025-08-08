//package com.alibaba.cloud.ai.graph.reactive.node;
//
//import com.alibaba.cloud.ai.graph.OverAllState;
//import com.alibaba.cloud.ai.graph.reactive.action.ReactiveNodeAction;
//import com.alibaba.cloud.ai.graph.reactive.config.QwenProperties;
//import com.alibaba.cloud.ai.graph.reactive.model.ChatChoice;
//import com.alibaba.cloud.ai.graph.reactive.model.ChatCompletions;
//import com.alibaba.cloud.ai.graph.reactive.model.ChatRole;
//import com.alibaba.cloud.ai.graph.reactive.model.OpenAiChatMessage;
//import com.alibaba.cloud.ai.graph.reactive.model.OpenAiChatParams;
//import com.alibaba.cloud.ai.graph.reactive.util.ReactiveChatClient;
//import com.alibaba.cloud.ai.graph.reactive.util.WebClientUtil;
//import com.google.common.collect.Lists;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class ReactiveLlmNode1 implements ReactiveNodeAction {
//
//    private final ReactiveChatClient chatClient;
//
//    private final String promptTemplate;
//
//    public ReactiveLlmNode(QwenProperties qwenProperties, String promptTemplate) {
//        this.chatClient = WebClientUtil.createReactiveChatClient(qwenProperties);
//        this.promptTemplate = promptTemplate;
//    }
//
//    @Override
//    public Mono<Map<String, Object>> apply(OverAllState state) {
//        return Mono.fromCallable(() -> (String) state.value("input").orElse(""))
//                .filter(input -> !input.isEmpty())
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Input text is empty")))
//                .flatMap(this::classifyText)
//                .map(result -> {
//                    Map<String, Object> updates = new HashMap<>();
//                    updates.put("output", result);
//                    return updates;
//                })
//                .onErrorReturn(Map.of("output", "unknown"));
//    }
//
//    private Mono<String> classifyText(String inputText) {
//        OpenAiChatParams openAiChatParams = OpenAiChatParams.builder()
//                .stream(true)
//                .temperature(1d)
//                .model("qwen-max")
//                .messages(Lists.newArrayList(
//                        OpenAiChatMessage.builder().role(ChatRole.SYSTEM.getValue()).content("你是一个有帮助的助手。").build(),
//                        OpenAiChatMessage.builder().role(ChatRole.USER.getValue()).content(inputText).build()
//                ))
//                .build();
//
//        return chatClient.chat(openAiChatParams)
//                .map(chatCompletions -> getContent(chatCompletions, true))
//                .filter(Objects::nonNull)
//                .filter(StringUtils::isNotEmpty)
//                .collect(Collectors.joining());
//    }
//
//    public static String getContent(ChatCompletions chatCompletions, boolean isContent) {
//        if (Objects.isNull(chatCompletions)) {
//            return null;
//        }
//        if (CollectionUtils.isEmpty(chatCompletions.getChoices())) {
//            return null;
//        }
//        Optional<ChatChoice> firstChatChoiceOptional = chatCompletions.getChoices().stream().findFirst();
//        if (firstChatChoiceOptional.isPresent()) {
//            ChatChoice chatChoice = firstChatChoiceOptional.get();
//            if (Objects.nonNull(chatChoice.getDelta())) {
//                return isContent ? chatChoice.getDelta().getContent() : chatChoice.getDelta().getReasoningContent();
//            } else if (Objects.nonNull(chatChoice.getMessage())) {
//                return chatChoice.getMessage().getContent();
//            }
//        }
//        return null;
//    }
//}
