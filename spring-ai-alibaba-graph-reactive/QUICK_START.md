# Spring AI Alibaba Graph Reactive - 快速开始

## 问题解决

### 1. Maven 版本问题
当前环境的 Maven 版本是 3.6.0，但项目需要 3.6.3+。

**解决方案：**
```bash
# 跳过代码检查插件来构建项目
mvn clean compile -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

### 2. 依赖问题修复

我已经修复了代码中的 `state.put()` 方法调用问题。原始的 `OverAllState` 类没有 `put` 方法，应该使用 `updateState()` 方法。

**修复内容：**
- `ReactiveCompiledGraph.java` 中的状态更新逻辑
- 使用 `state.updateState(updates)` 替代 `updates.forEach(state::put)`

## 项目结构

```
spring-ai-alibaba-graph-reactive/
├── spring-ai-alibaba-graph-reactive-core/      # 核心响应式组件
│   ├── src/main/java/com/alibaba/cloud/ai/graph/reactive/
│   │   ├── ReactiveStateGraph.java             # 响应式状态图
│   │   ├── ReactiveCompiledGraph.java          # 响应式编译图
│   │   ├── action/
│   │   │   ├── ReactiveNodeAction.java         # 响应式节点动作
│   │   │   └── ReactiveEdgeAction.java         # 响应式边动作
│   │   └── node/
│   │       └── ReactiveQuestionClassifierNode.java # 响应式分类节点
├── spring-ai-alibaba-graph-reactive-example/   # 示例应用
└── spring-ai-alibaba-graph-reactive-studio/    # 管理界面
```

## 核心 API 说明

### 1. ReactiveNodeAction
```java
@FunctionalInterface
public interface ReactiveNodeAction {
    Mono<Map<String, Object>> apply(OverAllState state);
}
```

### 2. ReactiveEdgeAction
```java
@FunctionalInterface
public interface ReactiveEdgeAction {
    Mono<String> apply(OverAllState state);
}
```

### 3. ReactiveStateGraph
```java
ReactiveStateGraph graph = new ReactiveStateGraph("MyGraph", stateFactory)
    .addNode("node1", nodeAction)
    .addEdge("node1", "node2")
    .addConditionalEdges("node2", edgeAction, mapping);
```

## 构建和运行

### 1. 构建项目
```bash
# 在项目根目录执行
mvn clean install -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

### 2. 运行示例
```bash
# 设置环境变量
export DASHSCOPE_API_KEY=your-api-key

# 启动应用
cd spring-ai-alibaba-graph-reactive/spring-ai-alibaba-graph-reactive-example
mvn spring-boot:run -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

### 3. 测试接口
```bash
# 单次响应式调用
curl "http://localhost:18081/reactive/customer/chat?query=这个产品很棒！"

# 流式响应调用
curl "http://localhost:18081/reactive/customer/chat/stream?query=产品有问题需要退货"
```

## 代码示例

### 创建响应式工作流
```java
@Configuration
public class ReactiveWorkflowConfig {
    
    @Bean
    public ReactiveStateGraph reactiveWorkflowGraph(ChatModel chatModel) {
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        // 创建响应式分类节点
        ReactiveQuestionClassifierNode classifier = 
            ReactiveQuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .categories(List.of("positive", "negative"))
                .build();

        // 创建响应式记录节点
        ReactiveRecordingNode recorder = new ReactiveRecordingNode();

        // 构建状态图
        return new ReactiveStateGraph("Reactive Workflow", () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());
            strategies.put("classifier_output", new ReplaceStrategy());
            strategies.put("solution", new ReplaceStrategy());
            return strategies;
        })
        .addNode("classifier", classifier)
        .addNode("recorder", recorder)
        .addEdge(ReactiveStateGraph.START, "classifier")
        .addEdge("classifier", "recorder")
        .addEdge("recorder", ReactiveStateGraph.END);
    }
}
```

### 创建响应式控制器
```java
@RestController
@RequestMapping("/reactive")
public class ReactiveController {
    
    private final ReactiveCompiledGraph graph;
    
    public ReactiveController(ReactiveStateGraph stateGraph) {
        this.graph = stateGraph.compile();
    }
    
    @GetMapping("/chat")
    public Mono<String> chat(@RequestParam String query) {
        return graph.invoke(Map.of("input", query))
                .map(state -> state.value("solution").toString());
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String query) {
        return graph.stream(Map.of("input", query))
                .map(state -> "State: " + state.toString());
    }
}
```

## 性能优势

与原版 Spring MVC 相比：
- **更高并发**：事件循环模型，单线程处理数千个连接
- **更低延迟**：非阻塞 I/O 减少等待时间
- **更少资源**：减少线程池大小，降低内存消耗
- **流式处理**：原生支持 Server-Sent Events
- **背压处理**：自动处理生产者消费者速度不匹配

## 故障排除

如果遇到问题，请参考：
- `TROUBLESHOOTING.md` - 详细的故障排除指南
- `README.md` - 完整的项目文档

## 下一步

1. 升级 Maven 到 3.6.3+ 版本以获得完整的开发体验
2. 配置 IDE 支持响应式编程调试
3. 编写单元测试验证响应式工作流
4. 监控应用性能和资源使用情况
