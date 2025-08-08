# Spring AI Alibaba Graph Reactive

## 项目概述

Spring AI Alibaba Graph Reactive 是基于 Spring WebFlux 的**响应式工作流和多智能体框架**，为 Java 开发者提供了构建复杂 AI 应用的响应式解决方案。它深度集成了 Spring Boot 生态系统，提供声明式 API 来编排响应式工作流。

与原版 Spring AI Alibaba Graph 相比，响应式版本具有以下优势：

- **非阻塞 I/O**：使用 Reactor 和 WebFlux 实现完全非阻塞的响应式编程
- **更高并发性**：能够处理更多并发请求，提高系统吞吐量
- **流式处理**：支持实时流式响应，适合长时间运行的 AI 工作流
- **背压处理**：内置背压机制，防止系统过载
- **资源高效**：更少的线程消耗，更高的资源利用率

## 核心概念与类

1. **ReactiveStateGraph**
   响应式状态图的主要类，用于定义工作流。
   支持添加节点（addNode）和边（addEdge、addConditionalEdges）。
   可编译为 ReactiveCompiledGraph 用于执行。

2. **ReactiveNodeAction**
   表示工作流中的单个步骤（如模型调用、数据转换）。
   返回 Mono<Map<String, Object>>，支持异步执行。

3. **ReactiveEdgeAction**
   表示节点间的转换逻辑。
   返回 Mono<String>，用于确定下一个节点。

4. **ReactiveCompiledGraph**
   ReactiveStateGraph 的可执行形式。
   处理实际执行、状态转换和结果流式输出。
   支持中断、并行节点和检查点。

## 项目结构

```
spring-ai-alibaba-graph-reactive/
├── spring-ai-alibaba-graph-reactive-core/          # 核心响应式组件
│   └── src/main/java/com/alibaba/cloud/ai/graph/reactive/
│       ├── ReactiveStateGraph.java                 # 响应式状态图
│       ├── ReactiveCompiledGraph.java             # 响应式编译图
│       ├── action/
│       │   ├── ReactiveNodeAction.java            # 响应式节点动作
│       │   └── ReactiveEdgeAction.java            # 响应式边动作
│       └── node/
│           └── ReactiveQuestionClassifierNode.java # 响应式分类节点
├── spring-ai-alibaba-graph-reactive-example/       # 响应式示例应用
│   └── src/main/java/com/alibaba/cloud/ai/example/graph/reactive/
│       ├── ReactiveGraphApplication.java           # 主应用类
│       └── workflow/
│           ├── ReactiveCustomerServiceController.java    # 响应式控制器
│           ├── ReactiveWorkflowAutoconfiguration.java   # 响应式配置
│           └── ReactiveRecordingNode.java               # 响应式记录节点
└── spring-ai-alibaba-graph-reactive-studio/        # 响应式管理界面
    └── src/main/java/com/alibaba/cloud/ai/studio/reactive/
        └── ReactiveGraphRunController.java         # 响应式运行控制器
```

## 快速开始

### 1. 添加依赖

在你的 Spring Boot 项目的 Maven `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-graph-reactive-core</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### 2. 配置模型 API 密钥

在 `application.yml` 中配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      api-key: ${DASHSCOPE_API_KEY}
```

### 3. 定义响应式工作流

```java
@Configuration
public class ReactiveWorkflowConfig {
    
    @Bean
    public ReactiveStateGraph reactiveWorkflowGraph(ChatModel chatModel) {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        ReactiveQuestionClassifierNode feedbackClassifier = 
            ReactiveQuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .categories(List.of("positive feedback", "negative feedback"))
                .build();

        return new ReactiveStateGraph("Reactive Workflow", stateFactory)
                .addNode("classifier", feedbackClassifier)
                .addNode("recorder", new ReactiveRecordingNode())
                .addEdge(ReactiveStateGraph.START, "classifier")
                .addEdge("classifier", "recorder")
                .addEdge("recorder", ReactiveStateGraph.END);
    }
}
```

### 4. 创建响应式控制器

```java
@RestController
@RequestMapping("/reactive")
public class ReactiveController {
    
    private final ReactiveCompiledGraph graph;
    
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

## 运行示例

### 启动应用

```bash
cd spring-ai-alibaba-graph-reactive/spring-ai-alibaba-graph-reactive-example
mvn spring-boot:run
```

### 测试响应式接口

```bash
# 单次调用
curl "http://localhost:18081/reactive/customer/chat?query=这个产品很棒！"

# 流式调用
curl "http://localhost:18081/reactive/customer/chat/stream?query=产品有问题需要退货"
```

## 响应式特性

### 1. 非阻塞执行
所有操作都是非阻塞的，使用 Mono 和 Flux 处理异步结果。

### 2. 流式响应
支持 Server-Sent Events (SSE) 进行实时流式响应。

### 3. 背压处理
内置背压机制，自动处理生产者和消费者速度不匹配的情况。

### 4. 错误处理
提供完善的响应式错误处理机制。

## 性能优势

- **更高并发**：单个线程可处理数千个并发连接
- **更低延迟**：非阻塞 I/O 减少等待时间
- **更少资源**：减少线程池大小，降低内存消耗
- **更好扩展**：适合微服务和云原生架构

## 与原版对比

| 特性 | 原版 (Spring MVC) | 响应式版本 (WebFlux) |
|------|------------------|---------------------|
| 编程模型 | 阻塞式 | 非阻塞响应式 |
| 并发处理 | 线程池 | 事件循环 |
| 内存使用 | 较高 | 较低 |
| 流式处理 | 有限支持 | 原生支持 |
| 背压处理 | 无 | 内置支持 |

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源协议。
