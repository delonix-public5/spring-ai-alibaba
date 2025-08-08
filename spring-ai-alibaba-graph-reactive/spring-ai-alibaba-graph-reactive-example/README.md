# Spring AI Alibaba Graph Reactive Example

## 如何运行

### 前置条件

1. 配置模型 API-KEY：

```shell
export DASHSCOPE_API_KEY=your-dashscope-api-key
```

### 启动应用

在 IDE 中直接运行 `ReactiveGraphApplication` 类启动示例应用。

或者，运行以下 maven 命令启动示例应用：

```shell
cd spring-ai-alibaba-graph-reactive/spring-ai-alibaba-graph-reactive-example
mvn spring-boot:run
```

应用将在端口 18081 启动（避免与原版冲突）。

## 响应式工作流示例（客户评价处理）

### 架构特点

响应式版本的客户评价处理系统具有以下特点：

1. **非阻塞处理**：所有操作都是异步非阻塞的
2. **流式响应**：支持实时流式返回中间状态
3. **背压处理**：自动处理生产者消费者速度不匹配
4. **高并发**：单线程可处理数千个并发请求

### 工作流程

1. **第一级分类节点**：将评论分为 positive 和 negative 两种（响应式执行）
2. **第二级分类节点**：对 negative 评论进行细分类（响应式执行）
3. **记录节点**：处理并记录最终结果（响应式执行）

### 测试接口

#### 单次响应式调用

```bash
# 正面评价
curl "http://localhost:18081/reactive/customer/chat?query=商品收到了，非常好，下次还会买。"

# 负面评价 - 产品质量问题
curl "http://localhost:18081/reactive/customer/chat?query=我收到的产品有质量问题，需要退换货？"

# 负面评价 - 物流问题
curl "http://localhost:18081/reactive/customer/chat?query=快递太慢了，等了一个星期才到。"

# 负面评价 - 售后服务
curl "http://localhost:18081/reactive/customer/chat?query=我的产品不能正常工作了，要怎么去做维修？"
```

#### 流式响应调用

```bash
# 流式处理，可以看到中间状态
curl "http://localhost:18081/reactive/customer/chat/stream?query=产品有问题需要退货"
```

流式响应会返回类似以下的 Server-Sent Events：

```
data: State: classifier=negative feedback, solution=
data: State: classifier=product quality, solution=
data: State: classifier=product quality, solution=product quality
```

## 性能对比

### 并发测试

使用 Apache Bench 进行并发测试：

```bash
# 测试响应式版本
ab -n 1000 -c 100 "http://localhost:18081/reactive/customer/chat?query=test"

# 对比原版
ab -n 1000 -c 100 "http://localhost:18080/customer/chat?query=test"
```

### 预期性能提升

- **吞吐量**：响应式版本通常有 2-5 倍的吞吐量提升
- **内存使用**：减少 30-50% 的内存消耗
- **响应时间**：在高并发下响应时间更稳定

## 监控和观测

### Actuator 端点

```bash
# 健康检查
curl http://localhost:18081/actuator/health

# 指标信息
curl http://localhost:18081/actuator/metrics

# 应用信息
curl http://localhost:18081/actuator/info
```

### 日志配置

应用配置了详细的响应式日志，可以观察到：

- Reactor 操作链的执行
- 背压处理情况
- 异步操作的调度
- 错误处理和恢复

## 开发建议

### 1. 响应式编程最佳实践

- 避免在响应式链中使用阻塞操作
- 使用 `subscribeOn()` 和 `publishOn()` 控制执行上下文
- 合理使用背压策略
- 注意异常处理和资源清理

### 2. 调试技巧

- 使用 `log()` 操作符观察数据流
- 使用 `checkpoint()` 标记调试点
- 配置详细的日志级别
- 使用 Reactor 调试工具

### 3. 性能优化

- 合理配置 Reactor Netty 参数
- 使用连接池复用连接
- 避免不必要的数据转换
- 使用缓存减少重复计算

## 更多示例

更多响应式示例请关注官网文档更新。
