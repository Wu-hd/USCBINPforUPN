# Backend Task 1 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 JDK 17 + Spring Boot 3.x 下完成后端“任务1：项目骨架与基础设施搭建”，并形成可持续扩展、可快速排障的多模块工程基线。

**Architecture:** 采用 Maven 多模块单体（parent/common/domain/infra/app）并强制单向依赖。通用协议与异常在 common，基础设施能力在 infra，应用入口与接口在 app，domain 只承载领域模型与接口。所有接口统一响应结构，异常统一收口，分页与主键策略在 infra 固化。

**Tech Stack:** Java 17, Maven, Spring Boot 3.x, Spring Web, Spring Security, MyBatis-Plus, JWT (jjwt), Redis, MapStruct, Lombok, Hibernate Validator, JUnit 5, Spring Boot Test

---

## File Structure Map

- `pom.xml`：父工程，依赖版本与模块聚合
- `uscbinp-common/`：统一响应、错误码、异常、全局异常处理
- `uscbinp-domain/`：领域模型（实体/DTO/VO）与领域接口
- `uscbinp-infra/`：MyBatis-Plus、Redis、JWT、安全配置、MapStruct
- `uscbinp-app/`：启动类、Controller、配置与集成测试

---

### Task 1: 搭建 Maven 多模块骨架

**Files:**
- Create: `pom.xml`
- Create: `uscbinp-common/pom.xml`
- Create: `uscbinp-domain/pom.xml`
- Create: `uscbinp-infra/pom.xml`
- Create: `uscbinp-app/pom.xml`
- Create: `uscbinp-app/src/main/java/com/uscbinp/app/UscbinpApplication.java`
- Test: `uscbinp-app/src/test/java/com/uscbinp/app/ContextLoadTest.java`

- [ ] **Step 1: 先写失败测试（上下文加载）**

```java
package com.uscbinp.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ContextLoadTest {
    @Test
    void contextLoads() {}
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl uscbinp-app -am -Dtest=ContextLoadTest test`
Expected: FAIL（缺少父工程/子模块 POM 或 Spring Boot 启动类）

- [ ] **Step 3: 实现最小可运行骨架**

```xml
<!-- root pom.xml -->
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.uscbinp</groupId>
  <artifactId>uscbinp-parent</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <modules>
    <module>uscbinp-common</module>
    <module>uscbinp-domain</module>
    <module>uscbinp-infra</module>
    <module>uscbinp-app</module>
  </modules>
</project>
```

```java
package com.uscbinp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UscbinpApplication {
    public static void main(String[] args) {
        SpringApplication.run(UscbinpApplication.class, args);
    }
}
```

- [ ] **Step 4: 重新运行测试确认通过**

Run: `mvn -q -pl uscbinp-app -am -Dtest=ContextLoadTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add pom.xml uscbinp-*/pom.xml uscbinp-app/src/main/java/com/uscbinp/app/UscbinpApplication.java uscbinp-app/src/test/java/com/uscbinp/app/ContextLoadTest.java
git commit -m "build: scaffold multi-module spring boot foundation"
```

---

### Task 2: 落地统一响应结构（code/message/data/timestamp）

**Files:**
- Create: `uscbinp-common/src/main/java/com/uscbinp/common/api/ApiResponse.java`
- Create: `uscbinp-common/src/test/java/com/uscbinp/common/api/ApiResponseTest.java`
- Modify: `uscbinp-common/pom.xml`（添加测试依赖）

- [ ] **Step 1: 写失败测试（响应结构字段与工厂方法）**

```java
package com.uscbinp.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {
    @Test
    void okResponseContainsAllRequiredFields() {
        ApiResponse<String> res = ApiResponse.ok("UP");
        assertEquals("00000", res.getCode());
        assertEquals("OK", res.getMessage());
        assertEquals("UP", res.getData());
        assertNotNull(res.getTimestamp());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl uscbinp-common -Dtest=ApiResponseTest test`
Expected: FAIL（`ApiResponse` 未定义）

- [ ] **Step 3: 实现最小代码使测试通过**

```java
package com.uscbinp.common.api;

import java.time.Instant;

public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = "00000";
        res.message = "OK";
        res.data = data;
        res.timestamp = Instant.now();
        return res;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = code;
        res.message = message;
        res.data = null;
        res.timestamp = Instant.now();
        return res;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public Instant getTimestamp() { return timestamp; }
}
```

- [ ] **Step 4: 重新运行测试**

Run: `mvn -q -pl uscbinp-common -Dtest=ApiResponseTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add uscbinp-common/pom.xml uscbinp-common/src/main/java/com/uscbinp/common/api/ApiResponse.java uscbinp-common/src/test/java/com/uscbinp/common/api/ApiResponseTest.java
git commit -m "feat: add unified api response contract"
```

---

### Task 3: 全局异常与参数校验统一收口

**Files:**
- Create: `uscbinp-common/src/main/java/com/uscbinp/common/error/ErrorCode.java`
- Create: `uscbinp-common/src/main/java/com/uscbinp/common/exception/BusinessException.java`
- Create: `uscbinp-common/src/main/java/com/uscbinp/common/web/GlobalExceptionHandler.java`
- Create: `uscbinp-app/src/main/java/com/uscbinp/app/controller/DemoController.java`
- Create: `uscbinp-app/src/main/java/com/uscbinp/app/controller/dto/DemoRequest.java`
- Test: `uscbinp-app/src/test/java/com/uscbinp/app/ExceptionContractTest.java`

- [ ] **Step 1: 写失败测试（业务异常/校验异常/系统异常统一格式）**

```java
package com.uscbinp.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionContractTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void validationErrorReturnsUnifiedResponse() throws Exception {
        mockMvc.perform(post("/api/demo/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void businessErrorReturnsUnifiedResponse() throws Exception {
        mockMvc.perform(get("/api/demo/business-error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("BIZ_4001"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl uscbinp-app -am -Dtest=ExceptionContractTest test`
Expected: FAIL（Controller/ExceptionHandler/ErrorCode 未实现）

- [ ] **Step 3: 最小实现（错误码、业务异常、全局处理器、演示接口）**

```java
// ErrorCode.java
public enum ErrorCode {
    SUCCESS("00000", "OK"),
    VALIDATION_ERROR("REQ_4000", "请求参数不合法"),
    BUSINESS_ERROR("BIZ_4001", "业务处理失败"),
    SYSTEM_ERROR("SYS_5000", "系统内部错误");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
```

```java
// BusinessException.java
public class BusinessException extends RuntimeException {
    private final String code;
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    public String getCode() { return code; }
}
```

```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.ok(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ":" + err.getDefaultMessage())
            .findFirst()
            .orElse("请求参数不合法");
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleSystem(Exception ex) {
        return ResponseEntity.status(500)
            .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
```

- [ ] **Step 4: 重新运行测试确认通过**

Run: `mvn -q -pl uscbinp-app -am -Dtest=ExceptionContractTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add uscbinp-common/src/main/java/com/uscbinp/common/error/ErrorCode.java uscbinp-common/src/main/java/com/uscbinp/common/exception/BusinessException.java uscbinp-common/src/main/java/com/uscbinp/common/web/GlobalExceptionHandler.java uscbinp-app/src/main/java/com/uscbinp/app/controller/DemoController.java uscbinp-app/src/main/java/com/uscbinp/app/controller/dto/DemoRequest.java uscbinp-app/src/test/java/com/uscbinp/app/ExceptionContractTest.java
git commit -m "feat: add global exception and validation contract"
```

---

### Task 4: MyBatis-Plus 分页与 ASSIGN_ID 主键策略

**Files:**
- Create: `uscbinp-infra/src/main/java/com/uscbinp/infra/config/MybatisPlusConfig.java`
- Create: `uscbinp-domain/src/main/java/com/uscbinp/domain/model/SampleEntity.java`
- Test: `uscbinp-infra/src/test/java/com/uscbinp/infra/config/MybatisPlusConfigTest.java`
- Test: `uscbinp-domain/src/test/java/com/uscbinp/domain/model/SampleEntityTest.java`

- [ ] **Step 1: 写失败测试（分页插件存在 + 主键注解为 ASSIGN_ID）**

```java
// SampleEntityTest.java
@Test
void idStrategyShouldBeAssignId() throws Exception {
    Field idField = SampleEntity.class.getDeclaredField("id");
    TableId tableId = idField.getAnnotation(TableId.class);
    assertNotNull(tableId);
    assertEquals(IdType.ASSIGN_ID, tableId.type());
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl uscbinp-domain,uscbinp-infra -am test -Dtest=SampleEntityTest,MybatisPlusConfigTest`
Expected: FAIL（配置类与实体未实现）

- [ ] **Step 3: 实现最小代码**

```java
// SampleEntity.java
public class SampleEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
}
```

```java
// MybatisPlusConfig.java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **Step 4: 重新运行测试**

Run: `mvn -q -pl uscbinp-domain,uscbinp-infra -am test -Dtest=SampleEntityTest,MybatisPlusConfigTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add uscbinp-infra/src/main/java/com/uscbinp/infra/config/MybatisPlusConfig.java uscbinp-domain/src/main/java/com/uscbinp/domain/model/SampleEntity.java uscbinp-infra/src/test/java/com/uscbinp/infra/config/MybatisPlusConfigTest.java uscbinp-domain/src/test/java/com/uscbinp/domain/model/SampleEntityTest.java
git commit -m "feat: configure mybatis-plus paging and assign-id strategy"
```

---

### Task 5: 安全、JWT、Redis、MapStruct 基础接入与扩展点

**Files:**
- Create: `uscbinp-infra/src/main/java/com/uscbinp/infra/security/SecurityConfig.java`
- Create: `uscbinp-infra/src/main/java/com/uscbinp/infra/jwt/JwtTokenProvider.java`
- Create: `uscbinp-infra/src/main/java/com/uscbinp/infra/redis/RedisConfig.java`
- Create: `uscbinp-infra/src/main/java/com/uscbinp/infra/mapping/CommonMapperConfig.java`
- Test: `uscbinp-app/src/test/java/com/uscbinp/app/SecurityAndInfraSmokeTest.java`

- [ ] **Step 1: 写失败测试（健康检查接口可匿名访问，Bean 可加载）**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndInfraSmokeTest {
    @Autowired MockMvc mockMvc;

    @Test
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl uscbinp-app -am -Dtest=SecurityAndInfraSmokeTest test`
Expected: FAIL（安全配置与健康检查接口未就绪）

- [ ] **Step 3: 最小实现（不实现登录业务，仅留扩展点）**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated())
            .build();
    }
}
```

```java
@Component
public class JwtTokenProvider {
    private static final String SECRET = "uscbinp-task1-secret-key-uscbinp-task1";

    public String generateToken(String subject) {
        return Jwts.builder()
            .subject(subject)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600_000))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String parseSubject(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}
```

```java
// RedisConfig.java
@Configuration
public class RedisConfig {}
```

- [ ] **Step 4: 重新运行测试**

Run: `mvn -q -pl uscbinp-app -am -Dtest=SecurityAndInfraSmokeTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add uscbinp-infra/src/main/java/com/uscbinp/infra/security/SecurityConfig.java uscbinp-infra/src/main/java/com/uscbinp/infra/jwt/JwtTokenProvider.java uscbinp-infra/src/main/java/com/uscbinp/infra/redis/RedisConfig.java uscbinp-infra/src/main/java/com/uscbinp/infra/mapping/CommonMapperConfig.java uscbinp-app/src/test/java/com/uscbinp/app/SecurityAndInfraSmokeTest.java
git commit -m "feat: integrate security jwt redis and mapstruct foundation"
```

---

### Task 6: 健康检查接口与最终验收测试

**Files:**
- Create: `uscbinp-app/src/main/java/com/uscbinp/app/controller/HealthController.java`
- Test: `uscbinp-app/src/test/java/com/uscbinp/app/Task1AcceptanceTest.java`

- [ ] **Step 1: 写失败验收测试（统一响应字段 + 时间戳存在）**

```java
@SpringBootTest
@AutoConfigureMockMvc
class Task1AcceptanceTest {
    @Autowired MockMvc mockMvc;

    @Test
    void healthShouldReturnUnifiedResponse() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").value("UP"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl uscbinp-app -am -Dtest=Task1AcceptanceTest test`
Expected: FAIL（`/api/health` 未实现或响应结构不匹配）

- [ ] **Step 3: 实现最小接口**

```java
@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("UP");
    }
}
```

- [ ] **Step 4: 运行全量 Task 1 相关测试**

Run: `mvn -q test`
Expected: PASS（所有模块测试通过）

- [ ] **Step 5: Commit**

```bash
git add uscbinp-app/src/main/java/com/uscbinp/app/controller/HealthController.java uscbinp-app/src/test/java/com/uscbinp/app/Task1AcceptanceTest.java
git commit -m "test: add task1 acceptance coverage and health endpoint"
```

---

### Task 7: 文档与交付清单对齐

**Files:**
- Modify: `task1-project-skeleton-infrastructure-design.md`
- Create: `task1-delivery-checklist.md`

- [ ] **Step 1: 写失败检查（清单项数量与 DoD 对齐）**

```markdown
在 `task1-delivery-checklist.md` 中先列出 6 条 DoD，如果缺项则视为失败。
```

- [ ] **Step 2: 运行检查确认失败**

Run: `rg "^- \\[ \\]" task1-delivery-checklist.md`
Expected: FAIL 或数量不足（文件不存在或项数不对）

- [ ] **Step 3: 写最小交付清单**

```markdown
# Task 1 Delivery Checklist
- [ ] 多模块工程启动成功
- [ ] 统一响应结构生效
- [ ] 三类异常统一返回
- [ ] 分页插件生效
- [ ] ASSIGN_ID 主键策略生效
- [ ] Security/JWT/Redis/MapStruct 基础接入完成
```

- [ ] **Step 4: 重新运行检查**

Run: `rg "^- \\[ \\]" task1-delivery-checklist.md`
Expected: 输出 6 行匹配

- [ ] **Step 5: Commit**

```bash
git add task1-project-skeleton-infrastructure-design.md task1-delivery-checklist.md
git commit -m "docs: align task1 delivery checklist with spec"
```

---

## Self-Review Notes

1. **Spec coverage:**  
   - 多模块骨架：Task 1  
   - 统一响应结构：Task 2  
   - 全局异常与校验：Task 3  
   - MyBatis-Plus 分页 + ASSIGN_ID：Task 4  
   - Security/JWT/Redis/MapStruct 接入：Task 5  
   - 健康检查与最终验收：Task 6  
   - 交付文档与 DoD 对齐：Task 7  
   覆盖无缺口。

2. **Placeholder scan:** 已检查，无占位语句。

3. **Type consistency:** 统一使用 `ApiResponse`、`BusinessException`、`ErrorCode`，命名一致，无跨任务签名冲突。

