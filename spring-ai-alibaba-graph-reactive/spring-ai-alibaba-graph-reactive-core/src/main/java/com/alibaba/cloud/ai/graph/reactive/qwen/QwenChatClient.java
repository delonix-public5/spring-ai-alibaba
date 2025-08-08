//package com.alibaba.cloud.ai.graph.reactive.qwen;
//
//import com.wormhole.agent.ai.core.service.AbstractChatClient;
//import com.wormhole.agent.ai.model.UnifiedModelEnum;
//import com.wormhole.agent.core.context.ModelContext;
//import com.wormhole.agent.core.model.ModelProviderEnum;
//import com.wormhole.agent.model.openai.OpenAiChatParams;
//import jakarta.annotation.Resource;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Flux;
//
//import java.util.Optional;
//
///**
// * QwenChatClient
// *
// * @author yangcanfeng
// * @version 2024/11/5
// */
//@Component
//public class QwenChatClient extends AbstractChatClient {
//
//    @Resource
//    private QwenChatModel qwenChatModel;
//
//    @Override
//    public boolean support(ModelContext modelContext) {
//        OpenAiChatParams openAiChatParams = modelContext.getOpenAiChatParams();
//        UnifiedModelEnum unifiedModelEnum = UnifiedModelEnum.findByModel(openAiChatParams.getModel(), openAiChatParams.getModelProvider());
//        return ModelProviderEnum.QWEN.getProvider().equalsIgnoreCase(Optional.ofNullable(unifiedModelEnum).map(UnifiedModelEnum::getProvider).orElse(null));
//    }
//
//    @Override
//    public Flux<String> processChatCompletions(OpenAiChatParams openAiChatParams, ModelContext modelContext) {
//        return qwenChatModel.chatCompletions(openAiChatParams);
//    }
//
//}
