# Enhanced Node I/O Mechanism

## 概述

spring-ai-alibaba-graph-reactive 现在支持增强的节点输入输出机制，允许每个节点访问之前所有节点的入参和出参。这为构建可视化工作流平台（类似 Dify、Coze 平台）提供了强大的基础。

## 主要特性

### 1. 节点输入输出参数
- **明确的入参出参概念**：每个节点都有明确的输入和输出参数
- **参数名重复支持**：不同节点可以有相同的参数名，通过节点ID区分
- **历史访问**：节点可以访问之前所有节点的输入和输出参数
- **类型安全**：支持类型安全的参数访问

### 2. 执行上下文管理
- **执行历史**：记录所有节点的执行顺序和参数历史
- **依赖关系**：自动管理节点间的依赖关系
- **元数据支持**：支持节点执行的元数据记录

### 3. 便捷的API
- **NodeIOResolver**：提供便捷的API来查询和访问参数
- **向后兼容**：与现有的ReactiveNodeAction完全兼容
- **流式支持**：支持流式执行和状态监控

## 核心组件

### ReactiveNodeInput
表示节点的输入参数，包含：
- 当前节点的直接输入参数
- 所有之前节点的输入历史
- 所有之前节点的输出历史

### ReactiveNodeOutput
表示节点的输出参数，包含：
- 节点产生的输出参数
- 全局状态更新
- 执行元数据

### EnhancedReactiveNodeAction
增强的节点动作接口，支持：
- 接收ReactiveNodeInput参数
- 返回ReactiveNodeOutput结果
- 向后兼容原有接口

### NodeIOResolver
工具类，提供便捷的API：
- 访问当前节点的输入参数
- 访问之前节点的输入输出参数
- 类型安全的参数访问
- 参数查询和过滤

## 使用示例

### 1. 创建增强节点

```java
public class DataProcessorNode implements EnhancedReactiveNodeAction {
    @Override
    public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
        return Mono.fromCallable(() -> {
            NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
            
            // 获取输入参数
            String input = resolver.getInput("input", String.class).orElse("");
            
            // 处理数据
            String processed = input.toUpperCase();
            int wordCount = input.split("\\s+").length;
            
            // 返回输出
            return ReactiveNodeOutput.builder("data_processor")
                    .output("processed_text", processed)
                    .output("word_count", wordCount)
                    .stateUpdate("processing_step", "completed")
                    .metadata("processing_time", System.currentTimeMillis())
                    .build();
        });
    }
}
```

### 2. 访问之前节点的输出

```java
public class DataTransformerNode implements EnhancedReactiveNodeAction {
    @Override
    public Mono<ReactiveNodeOutput> applyEnhanced(OverAllState state, ReactiveNodeInput nodeInput) {
        return Mono.fromCallable(() -> {
            NodeIOResolver resolver = NodeIOResolver.of(nodeInput, null);
            
            // 获取之前节点的输出
            String processedText = resolver.getOutput("data_processor", "processed_text")
                    .map(Object::toString).orElse("");
            Integer wordCount = resolver.getOutput("data_processor", "word_count", Integer.class)
                    .orElse(0);
            
            // 转换数据
            String transformed = processedText.replace(" ", "_");
            
            return ReactiveNodeOutput.builder("data_transformer")
                    .output("transformed_text", transformed)
                    .output("original_word_count", wordCount)
                    .build();
        });
    }
}
```

### 3. 构建工作流图

```java
ReactiveStateGraph graph = new ReactiveStateGraph(
        "Enhanced Workflow",
        () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());
            strategies.put("result", new ReplaceStrategy());
            return strategies;
        })
        .addEnhancedNode("processor", new DataProcessorNode())
        .addEnhancedNode("transformer", new DataTransformerNode())
        .addEnhancedNode("aggregator", new DataAggregatorNode())
        
        .addEdge(ReactiveStateGraph.START, "processor")
        .addEdge("processor", "transformer")
        .addEdge("transformer", "aggregator")
        .addEdge("aggregator", ReactiveStateGraph.END);

ReactiveCompiledGraph compiledGraph = graph.compile();
```

### 4. 执行工作流

```java
// 执行工作流
Mono<OverAllState> result = compiledGraph.invoke(Map.of("input", "Hello World"));

// 流式执行
Flux<OverAllState> stream = compiledGraph.stream(Map.of("input", "Hello World"));

// 获取执行上下文
ReactiveNodeExecutionContext context = compiledGraph.getExecutionContext();
List<String> executionOrder = context.getExecutionOrder();
Map<String, Map<String, Object>> outputHistory = context.getAllNodeOutputHistory();
```

## 参数名重复处理

系统支持不同节点使用相同的参数名，通过节点ID进行区分：

```java
// 节点A输出 "data" 参数
ReactiveNodeOutput.builder("nodeA").output("data", "valueA").build();

// 节点B也输出 "data" 参数
ReactiveNodeOutput.builder("nodeB").output("data", "valueB").build();

// 节点C可以分别访问两个节点的 "data" 参数
String dataFromA = resolver.getOutput("nodeA", "data", String.class).orElse("");
String dataFromB = resolver.getOutput("nodeB", "data", String.class).orElse("");
```

## 向后兼容性

新的增强机制完全向后兼容：

```java
// 原有的ReactiveNodeAction仍然可以使用
ReactiveNodeAction oldNode = state -> Mono.just(Map.of("result", "value"));

// 可以混合使用增强节点和普通节点
graph.addNode("oldNode", oldNode)
     .addEnhancedNode("newNode", enhancedNode);
```

## 测试示例

项目包含完整的测试示例：
- `EnhancedNodeIOTest.java` - 单元测试
- `EnhancedWorkflowExample.java` - 完整示例
- `EnhancedWorkflowController.java` - REST API测试

## API端点

启动应用后，可以通过以下端点测试功能：

- `GET /reactive/enhanced/workflow?input=test` - 测试增强工作流
- `GET /reactive/enhanced/workflow/stream?input=test` - 流式执行
- `GET /reactive/enhanced/workflow/context?input=test` - 获取执行上下文
- `GET /reactive/enhanced/workflow/collision-test?input=test` - 测试参数名重复

## 适用场景

这个增强的节点I/O机制特别适合：

1. **可视化工作流平台**：支持复杂的节点间数据传递
2. **数据处理管道**：每个节点可以访问完整的处理历史
3. **AI工作流**：支持复杂的AI任务编排和数据流转
4. **业务流程自动化**：支持复杂的业务逻辑和数据依赖

## 性能考虑

- 执行上下文会保存所有节点的输入输出历史，对于长时间运行的工作流需要考虑内存使用
- 可以通过 `compiledGraph.clearExecutionContext()` 清理执行上下文
- 支持执行上下文的快照功能，便于状态保存和恢复
