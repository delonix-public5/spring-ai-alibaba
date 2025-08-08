# 故障排除指南

## ✅ 已修复的问题

### 1. OverAllState.put() 方法问题
**问题**：`state.put()` 方法不存在，导致编译错误
**解决**：已修复所有文件中的 `state.put()` 调用，改为使用 `state.updateState()` 方法

**修复的文件：**
- `ReactiveCompiledGraph.java` - 第84行和其他位置
- `ReactiveGraphRunController.java` - createErrorState 方法

### 2. 响应式线程阻塞问题
**问题**：`block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-4`
**解决**：在 `ReactiveQuestionClassifierNode` 中使用 `subscribeOn(Schedulers.boundedElastic())` 将阻塞调用移到弹性调度器

**修复的文件：**
- `ReactiveQuestionClassifierNode.java` - classifyText 方法

## Maven 版本问题

### 问题描述
```
Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:3.5.0:check
The plugin org.apache.maven.plugins:maven-checkstyle-plugin:3.5.0 requires Maven version 3.6.3
```

### 解决方案

#### 方案1：升级 Maven 版本（推荐）
1. 下载 Maven 3.6.3 或更高版本
2. 更新环境变量 `MAVEN_HOME` 和 `PATH`
3. 验证版本：`mvn --version`

#### 方案2：跳过代码检查插件
如果暂时无法升级 Maven，可以使用以下命令跳过代码检查：

```bash
# 编译项目
mvn clean compile -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true

# 安装到本地仓库
mvn clean install -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

#### 方案3：使用 Maven Wrapper
项目根目录提供了 Maven Wrapper，可以使用：

```bash
# Linux/Mac
./mvnw clean compile -DskipTests

# Windows
mvnw.cmd clean compile -DskipTests
```

## 依赖问题

### 问题描述
```
com.alibaba.cloud.ai:spring-ai-alibaba-graph-reactive-core:jar:1.0.0.3-SNAPSHOT was not found
```

### 解决方案

#### 1. 首先构建父项目
```bash
# 在项目根目录执行
mvn clean install -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

#### 2. 然后构建响应式模块
```bash
cd spring-ai-alibaba-graph-reactive
mvn clean install -DskipTests -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

#### 3. 强制更新依赖
如果仍有问题，可以强制更新：
```bash
mvn clean install -DskipTests -U -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

## 运行时问题

### 启动应用
```bash
cd spring-ai-alibaba-graph-reactive/spring-ai-alibaba-graph-reactive-example
mvn spring-boot:run -Dcheckstyle.skip=true -Dspring-javaformat.skip=true
```

### 环境变量配置
确保设置了必要的环境变量：
```bash
export DASHSCOPE_API_KEY=your-api-key
```

## IDE 配置

### IntelliJ IDEA
1. 导入项目时选择 Maven 项目
2. 在 Settings > Build > Build Tools > Maven 中设置正确的 Maven 版本
3. 在 Settings > Editor > Code Style > Java 中禁用 Checkstyle

### Eclipse
1. 导入为 Maven 项目
2. 右键项目 > Properties > Java Build Path > Libraries
3. 确保 Maven Dependencies 正确加载

## 常见错误

### 1. 端口冲突
如果 18081 端口被占用，修改 `application.yml` 中的端口：
```yaml
server:
  port: 18082
```

### 2. API Key 未配置
确保在 `application.yml` 或环境变量中配置了正确的 API Key。

### 3. 网络问题
如果无法下载依赖，可以配置阿里云镜像：
```xml
<mirror>
    <id>alimaven</id>
    <name>aliyun maven</name>
    <url>https://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>
</mirror>
```

## 联系支持

如果以上方案都无法解决问题，请提供以下信息：
1. Maven 版本：`mvn --version`
2. Java 版本：`java --version`
3. 操作系统版本
4. 完整的错误日志
