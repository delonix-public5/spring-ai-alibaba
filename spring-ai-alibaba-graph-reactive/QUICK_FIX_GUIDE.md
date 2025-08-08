# 快速修复指南

## 🔧 已修复的问题

### 1. OverAllState.put() 方法问题 ✅
- **问题**：`state.put()` 方法不存在
- **修复**：使用 `state.updateState()` 方法
- **状态**：已完全修复

### 2. 响应式线程阻塞问题 ✅
- **问题**：`block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-4`
- **修复**：使用 `subscribeOn(Schedulers.boundedElastic())` 将阻塞调用移到弹性调度器
- **状态**：已修复

## 🚀 快速测试方法

### 方法1：使用 IDE 运行（推荐）

1. **在 IntelliJ IDEA 中：**
   - 导入项目：File → Open → 选择 `spring-ai-alibaba` 目录
   - 等待 Maven 依赖下载完成
   - 找到 `ReactiveGraphApplication.java` 文件
   - 右键 → Run 'ReactiveGraphApplication'

2. **设置环境变量：**
   - 在 Run Configuration 中添加环境变量：
   - `DASHSCOPE_API_KEY=your-api-key`

3. **测试接口：**
   ```bash
   curl "http://localhost:18081/reactive/customer/chat?query=这个产品很棒！"
   ```

### 方法2：Maven 命令行（需要 Maven 3.6.3+）

```bash
# 1. 构建项目
mvn clean install -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true

# 2. 运行应用
cd spring-ai-alibaba-graph-reactive/spring-ai-alibaba-graph-reactive-example
mvn spring-boot:run -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

### 方法3：跳过父项目插件

如果 Maven 版本问题持续存在，可以：

1. **临时禁用插件：**
   在响应式项目的 pom.xml 中已经添加了插件跳过配置

2. **使用 Maven Wrapper：**
   ```bash
   ./mvnw clean install -DskipTests
   ```

## 🧪 测试用例

### 1. 正面评价测试
```bash
curl "http://localhost:18081/reactive/customer/chat?query=这个产品很棒，质量很好！"
```
**预期结果**：`"Praise, no action taken."`

### 2. 负面评价测试
```bash
curl "http://localhost:18081/reactive/customer/chat?query=产品有质量问题，需要退货"
```
**预期结果**：包含具体的问题分类

### 3. 流式响应测试
```bash
curl "http://localhost:18081/reactive/customer/chat/stream?query=物流太慢了"
```
**预期结果**：Server-Sent Events 流式响应

## 🔍 验证修复效果

### 1. 检查日志
启动应用后，查看日志中是否有：
- ✅ 没有 `state.put()` 相关错误
- ✅ 没有 `blocking` 相关错误
- ✅ 响应式工作流正常执行

### 2. 检查响应
- ✅ API 返回正确的分类结果
- ✅ 流式接口正常工作
- ✅ 没有线程阻塞异常

## 📊 性能验证

### 并发测试
```bash
# 使用 Apache Bench 测试并发性能
ab -n 100 -c 10 "http://localhost:18081/reactive/customer/chat?query=test"
```

### 内存监控
```bash
# 查看应用内存使用
curl http://localhost:18081/actuator/metrics/jvm.memory.used
```

## 🐛 如果仍有问题

### 1. 检查环境
- Java 版本：`java --version` (需要 17+)
- Maven 版本：`mvn --version` (推荐 3.6.3+)
- 环境变量：确保 `DASHSCOPE_API_KEY` 已设置

### 2. 清理缓存
```bash
# 清理 Maven 缓存
mvn dependency:purge-local-repository

# 清理 IDE 缓存（IntelliJ）
File → Invalidate Caches and Restart
```

### 3. 查看详细日志
```bash
# 启用调试日志
java -jar target/app.jar --logging.level.com.alibaba.cloud.ai=DEBUG
```

## 📞 支持

如果问题仍然存在，请提供：
1. 完整的错误堆栈
2. Java 和 Maven 版本
3. 操作系统信息
4. 是否设置了 API Key

---

**状态**：✅ 核心问题已修复，可以正常使用
**最后更新**：2025-08-07
