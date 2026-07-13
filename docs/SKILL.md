---
name: spring-boot
description: Spring Boot 后端开发规范。当开发 Spring Boot 项目、实现 REST API、MyBatis-Plus 数据访问、权限认证、JWT/Redis Token 时使用此 skill。
---

# Spring Boot 后端开发规范

## 触发条件

- 开发 Spring Boot 项目
- 实现 REST API
- 使用 MyBatis-Plus 数据访问
- 实现 JWT/Redis Token 认证
- 实现权限控制

---

## Part 1: 技术栈

- **JDK21** - Java 运行环境
- **Spring Boot3** - 基础框架
- **MyBatis-Plus** - ORM 框架
- **MySQL8** - 数据库
- **Redis** - 缓存redisson的RedissonClient
- **JWT** - Token 认证
- **springdoc** - API 文档

---

## Part 2: 目录结构

```
src/main/java/com/youlai/
├── common/                     # 公共模块
│   ├── annotation/             # 自定义注解
│   │   ├── RepeatSubmit.java
│   │   └── DataScope.java
│   ├── aspect/                 # 切面
│   │   └── LogAspect.java
│   ├── config/                 # 配置类
│   │   ├── MybatisPlusConfig.java
│   │   ├── RedisConfig.java
│   │   └── SecurityConfig.java
│   ├── constant/               # 常量
│   │   ├── SecurityConstants.java
│   │   └── RedisConstants.java
│   ├── enums/                  # 枚举
│   │   ├── ResultCodeEnum.java
│   │   └── MenuTypeEnum.java
│   ├── exception/              # 异常
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── model/                  # 基础模型
│   │   ├── BaseEntity.java
│   │   ├── PageQuery.java
│   │   └── Result.java
│   └── util/                   # 工具类
│       ├── JwtUtils.java
│       └── SecurityUtils.java
├── module/                     # 业务模块
│   ├── system/                 # 系统模块
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── converter/          # 对象转换
│   │   │   └── UserConverter.java
│   │   ├── entity/
│   │   │   └── SysUser.java
│   │   ├── mapper/
│   │   │   └── SysUserMapper.java
│   │   ├── service/
│   │   │   ├── IUserService.java
│   │   │   └── impl/UserServiceImpl.java
│   │   ├── query
│           ├── UserPageQuery.java
│   │   ├── converter
│           ├── UserConverter.java
│   │   └── vo
│   │       └── UserVO.java
│   └── auth/                   # 认证模块
│       ├── controller/
│       │   └── AuthController.java
│       ├── service/
│       │   └── IAuthService.java
│           └── impl/AuthServiceImpl.java    
│       └── dto/
│           ├── LoginForm.java
│           └── LoginResult.java
├── security/                   # 安全模块
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java
│   ├── handler/
│   │   └── AuthenticationEntryPointImpl.java
│   └── model/
│       └── SysUserDetails.java
└── Application.java            # 启动类
```

---

## Part 3: 命名规范

### 类命名

| 类型        | 规范                       | 示例                                  |
| ----------- | -------------------------- | ------------------------------------- |
| 实体        | 实体名 + Entity 或前缀 Sys | `User` 或 `SysUser`                   |
| DTO         | 功能 + DTO 类型            | `UserVO`, `UserForm`, `UserPageQuery` |
| Service     | 实体 + Service             | `UserService`                         |
| ServiceImpl | 实体 + ServiceImpl         | `UserServiceImpl`                     |
| Controller  | 实体 + Controller          | `UserController`                      |
| Mapper      | 实体 + Mapper              | `UserMapper`                          |
| Converter   | 实体 + Converter           | `UserConverter`                       |

### 方法命名

| 动作     | 前缀          | 示例                 |
| -------- | ------------- | -------------------- |
| 查询单个 | get           | `getById()`          |
| 查询列表 | list          | `listUsers()`        |
| 分页查询 | page          | `pageUsers()`        |
| 新增     | save/add      | `saveUser()`         |
| 更新     | update        | `updateUser()`       |
| 删除     | remove/delete | `removeById()`       |
| 统计     | count         | `countUsers()`       |
| 判断存在 | exists        | `existsByUsername()` |

### 变量命名

| 类型     | 规范             | 示例                         |
| -------- | ---------------- | ---------------------------- |
| 成员变量 | camelCase        | `userService`                |
| 常量     | UPPER_SNAKE_CASE | `MAX_SIZE`                   |
| 私有变量 | _ 前缀或无前缀  | `_internalState`             |
| 布尔值   | is/has/can 前缀  | `isDeleted`, `hasPermission` |

---

## Part 4: RESTful API 规范

### 路径命名

- 使用 kebab-case：`/api/v1/user-profile`
- 版本前缀：`/api/v1/`
- 资源命名：复数名词 `/users`, `/roles`

### 标准 CRUD 路径

| 操作     | 方法   | 路径                      |
| -------- | ------ | ------------------------- |
| 分页列表 | GET    | `/api/v1/users/page`      |
| 详情     | GET    | `/api/v1/users/{id}`      |
| 新增     | POST   | `/api/v1/users`           |
| 更新     | PUT    | `/api/v1/users`           |
| 删除     | DELETE | `/api/v1/users/{id}`      |
| 批量删除 | DELETE | `/api/v1/users/batch`     |
| 下拉选项 | GET    | `/api/v1/users/options`   |
| 表单数据 | GET    | `/api/v1/users/{id}/form` |

### Controller 模板

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "用户分页列表")
    public Result<PageResult<UserVO>> page(UserPageQuery query) {
        return Result.success(userService.pageUsers(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/{id}/form")
    @Operation(summary = "用户表单数据")
    public Result<UserForm> getFormData(@PathVariable Long id) {
        return Result.success(userService.getUserFormData(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('sys:user:add')")
    public Result<Long> save(@Valid @RequestBody UserForm form) {
        return Result.success(userService.saveUser(form));
    }

    @PutMapping
    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<Void> update(@Valid @RequestBody UserForm form) {
        userService.updateUser(form);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<Void> remove(@PathVariable Long id) {
        userService.removeUserById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        userService.removeUsersByIds(ids);
        return Result.success();
    }

    @GetMapping("/options")
    @Operation(summary = "用户选项列表")
    public Result<List<OptionType>> options() {
        return Result.success(userService.listUserOptions());
    }
}
```

---

## Part 5: 响应格式

### 统一响应类

```java
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed(ResultCode code) {
        Result<T> result = new Result<>();
        result.setCode(code.getCode());
        result.setMsg(code.getMsg());
        return result;
    }

    public static <T> Result<T> failed(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
```

### 分页响应类

```java
@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;

    public static <T> PageResult<T> of(List<T> list, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        return result;
    }
}
```

### 响应码枚举

```java
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统异常");

    private final Integer code;
    private final String msg;
}
```

---

## Part 6: 实体规范

### 基础实体

```java
@Data
public class BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableLogic
    private Integer deleted;
}
```

### 实体示例

#### MyBatis Plus 实体规范
 - 实体类使用 @TableName 注解映射表名
 - 主键字段使用 @TableId 注解，指定主键策略
 - 非数据库字段使用 @TableField(exist = false) 标注
 - 逻辑删除字段使用 @TableLogic 注解
 - 自动填充字段（创建时间、更新时间）使用 @TableField(fill = FieldFill.INSERT) 或 FieldFill.INSERT_UPDATE
 - 实现 MetaObjectHandler 接口处理自动填充
 - 实体类使用 Lombok：@Data、@NoArgsConstructor、@AllArgsConstructor

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户实体")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别(0未知 1男 2女)")
    private Integer gender;

    @Schema(description = "状态(1正常 0禁用)")
    private Integer status;

    @Schema(description = "部门ID")
    private Long deptId;
}
```

---

## Part 7: Service 规范

### Service 接口

```java
public interface UserService extends IService<SysUser>{
    PageResult<UserVO> pageUsers(UserPageQuery query);
    UserVO getUserById(Long id);
    UserForm getUserFormData(Long id);
    Long saveUser(UserForm form);
    void updateUser(UserForm form);
    void removeUserById(Long id);
    void removeUsersByIds(List<Long> ids);
    List<OptionType> listUserOptions();
}
```

### Service 实现

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final UserConverter userConverter;
    private final ISysUserRoleService sysUserRoleService;

    @Override
    public PageResult<UserVO> pageUsers(UserPageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StrUtil.isNotBlank(query.getKeywords()), SysUser::getUsername, query.getKeywords())
               .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
               .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(page, wrapper);
        List<UserVO> list = userConverter.toVOList(result.getRecords());
        return PageResult.of(list, result.getTotal());
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        Assert.notNull(user, "用户不存在");
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveUser(UserForm form) {
        // 检查用户名是否存在
        Assert.isTrue(!existsByUsername(form.getUsername()), "用户名已存在");
        // 保存用户
        SysUser user = userConverter.toEntity(form);
        user.setPassword(BCryptUtils.encode(form.getPassword()));
        save(user);
        // 保存用户角色
        sysUserRoleService.insertBatch(user.getId(), form.getRoleIds());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserForm form) {
        SysUser user = userConverter.toEntity(form);
        updateById(user);
        // 更新用户角色
        sysUserRoleService.deleteByUserId(user.getId());
        sysUserRoleService.insertBatch(user.getId(), form.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserById(Long id) {
        removeById(id);
        sysUserRoleService.deleteByUserId(id);
    }

    private boolean existsByUsername(String username) {
        return exists(Wrappers.lambdaQuery(SysUser.class)
            .eq(SysUser::getUsername, username));
    }
}
```

---

## Part 8: 认证规范

### JWT 配置

```yaml
security:
  session:
    mode: jwt # 或 redis-token
    jwt:
      secret: SecretKey012345678901234567890123456789
      issuer: youlai-boot
    access-token-ttl: 7200 # 2小时
    refresh-token-ttl: 604800 # 7天
```

### Token 生成

```java
@Component
@RequiredArgsConstructor
public class JwtTokenManager {

    @Value("${security.session.jwt.secret}")
    private String secret;

    @Value("${security.session.jwt.issuer}")
    private String issuer;

    public String generateAccessToken(SysUserDetails userDetails) {
        return Jwts.builder()
            .subject(String.valueOf(userDetails.getUserId()))
            .claim("username", userDetails.getUsername())
            .issuer(issuer)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenTtl * 1000))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .compact();
    }

    public SysUserDetails parseAccessToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return SysUserDetails.builder()
            .userId(Long.parseLong(claims.getSubject()))
            .username(claims.get("username", String.class))
            .build();
    }
}
```

### 权限注解

```java
// 按钮权限
@PreAuthorize("hasAuthority('sys:user:add')")

// 角色权限
@PreAuthorize("hasRole('ADMIN')")

// 数据权限
@DataScope(deptAlias = "d", userAlias = "u")
```

---

## Part 9: 异常处理

### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.failed(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failed(ResultCode.SYSTEM_ERROR);
    }
}
```

### 业务异常

```java
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.PARAM_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode code) {
        super(code.getMsg());
        this.code = code.getCode();
    }
}
```

---

## Part 10: 代码质量检查清单

- [ ] 遵循 RESTful API 路径规范
- [ ] 使用统一响应格式
- [ ] 实现全局异常处理
- [ ] 添加权限注解
- [ ] 分页接口使用 PageResult
- [ ] 参数校验使用 @Valid
- [ ] 重要操作记录日志
- [ ] 使用 @Transactional 处理事务
- [ ] 实体继承 BaseEntity
- [ ] 使用 MyBatis-Plus 逻辑删除
- [ ] API 添加 @Operation 注解
- [ ] Service 接口与实现分离
- [ ] 日志记录 (SLF4J)使用@Slf4j注解

## Part 11: 注释规范
- 所有类、接口必须有中文 Javadoc 注释，说明用途和职责
- 所有 public 方法必须有中文 Javadoc 注释，包含功能说明、@param、@return、@throws
- 复杂业务逻辑、核心算法必须添加中文行内注释说明意图
- ODO 注释格式：// TODO: [作者] 具体待办事项描述
- 禁止无意义注释，注释必须与代码保持同步
- Mapper XML 中的自定义 SQL 必须添加中文注释说明查询用途
- 注释掉的代码应直接删除，版本管理由 Git 负责，禁止保留注释代码

## Part 12: 接口与返回值规范
- 统一使用 RESTful 风格接口，API 版本化管理：/api/v1/...
- 使用自定义 Result<T> 统一返回，包含 code、message、data，成功码固定为 0，失败使用5位分段编码（如 10001、20001），绝大部分接口返回 HTTP 200
- Controller 接口添加 Swagger/OpenAPI 注解：@Tag、@Operation
- 请求参数接收：
- JSON 请求体使用 @RequestBody
- 查询参数使用 @RequestParam
- 路径参数使用 @PathVariable
- 参数校验必须完善：@NotBlank、@NotNull、@Size、@Valid 等

## Part 13: 日志规范
- 使用 @Slf4j（Lombok）+ Logback，禁止使用 System.out.println()
- 禁止使用 e.printStackTrace()，必须通过日志框架记录异常
- 日志信息必须使用中文，参数化格式：log.info("用户 {} 登录成功", userId)
- 日志级别使用规范：
- ERROR：系统异常、不可恢复错误，必须附带完整异常栈
- WARN：潜在问题、业务异常、降级处理
- INFO：关键业务节点（用户登录、订单创建、支付完成等）
- DEBUG：调试信息，生产环境关闭
- 绝不记录敏感数据（密码、令牌、身份证号、银行卡号）
- 异常日志必须包含上下文信息：log.error("创建订单失败，用户ID：{}", userId, e)
- 生产环境禁止使用 DEBUG 级别日志输出

## Part 13: 异常规范
- 自定义异常必须使用中文错误信息
- 建立异常层次结构：BaseException -> NotFoundException, ValidationException, BusinessException
- 异常类必须包含错误码（code）和中文错误信息（message）
- 业务校验失败抛出 BusinessException，资源不存在抛出 NotFoundException
- 使用 @RestControllerAdvice 做全局异常捕获和处理
- 禁止在 Controller 层捕获异常后返回错误响应，统一由全局异常处理器处理
- @Transactional 必须指定 rollbackFor = Exception.class
- 捕获异常时禁止空 catch 块，至少记录日志
- 返回类型为基本数据类型时，return 包装数据类型对象可能自动拆箱产生 NPE，必须判空
- 集合元素即使 isNotEmpty，取出的数据元素也可能为 null，使用前必须判空

## Part 14: 格式规范
- 统一使用 4 空格缩进，禁止 Tab
- 单行代码长度不超过 120 字符
- 方法体长度不超过 80 行，超过必须拆分
- 方法参数不超过 5 个，超过使用对象封装
- 大括号不换行（K&R 风格），一行一条语句
- if/else/for/while/do 语句必须使用大括号，即使只有一行代码
- 类成员排列顺序：静态变量 → 实例变量 → 构造方法 → 公有方法 → 私有方法
- 方法内部定义的局部变量应在首次使用时声明，不要集中在方法顶部

## Part 15: OOP 规约
- 所有覆写方法必须加 @Override 注解
- 构造方法里禁止加入业务逻辑，如有初始化逻辑应放在 @PostConstruct 或 init 方法中
- POJO 类必须写 toString 方法，便于排查问题
- 可变参数必须放在参数列表最后
- 避免通过类的对象引用来访问静态变量或方法，应直接使用类名访问
- equals 方法必须满足对称性、传递性、一致性，重写 equals 必须同时重写 hashCode
- 优先使用基本数据类型而非包装类型，避免自动拆箱导致的 NPE

## Part 16: 集合处理
- ArrayList 的 subList 结果不可强转成 ArrayList，否则抛出 ClassCastException
- 使用 Map 的 entrySet 遍历键值对，keySet 仅遍历键
- Map 的 key 为自定义对象时必须重写 hashCode 和 equals
- foreach 循环里禁止进行元素的 remove/add 操作，应使用 Iterator
- Arrays.asList() 返回的集合不能使用 add/remove/clear 方法
- 集合转数组必须使用 toArray(T[] array)，避免类型转换异常
- 泛型通配符：<? extends T> 上界通配符不能 add，<? super T> 下界通配符不能 get

## Part 17: 代码质量强制要求
- 禁止空指针：所有可能为 null 的返回值必须判空或使用 Optional，禁止信任外部输入
- 禁止魔法值：代码中不允许出现未解释的硬编码常量，必须定义为命名常量或枚举
- 禁止：if (status == 1)
- 正确：if (status == UserStatusEnum.ACTIVE.getCode())
- 禁止 == 比较对象值，字符串使用 equals()，对象使用 Objects.equals()
- 集合操作前必须判空，使用 CollectionUtils.isEmpty() 或 CollUtil.isEmpty()
- 数值计算注意溢出和精度，金额必须使用 BigDecimal，禁止使用 float/double
- 日期时间使用 LocalDateTime，禁止使用 java.util.Date
- 禁止在循环中拼接字符串，使用 StringBuilder
- 禁止在循环中执行数据库操作，使用批量方法
- switch 语句必须包含 default 分支
- 所有资源（流、连接）必须使用 try-with-resources 关闭
- 并发场景必须使用线程安全集合或加锁，禁止在多线程中使用非安全集合
- 优先使用 JDK8+ 特性：Stream、Lambda、Optional
- 方法入参必须校验：外部传入参数、跨层调用参数、public 方法入参
- 优先使用卫语句减少 if-else 嵌套，尽早 return

## Part 18:  并发处理
- 线程资源必须通过线程池提供，禁止显式 new Thread()
- 线程池禁止使用 Executors 创建，必须通过 ThreadPoolExecutor 方式，明确线程池参数
- 必须回收自定义 ThreadLocal 变量，使用 try-finally 调用 remove()，避免内存泄漏
- 多线程加锁必须保持一致的加锁顺序，避免死锁
- SimpleDateFormat 线程不安全，使用 DateTimeFormatter 替代
- 并发修改同一记录时必须加锁，避免数据不一致
- volatile 解决多线程内存可见性问题，但不能保证原子性，原子操作使用 Atomic 类

## Part 19:  最佳实践
- 始终使用构造器注入（@RequiredArgsConstructor）
- 使用 DTO 进行请求/响应，绝不直接暴露实体类
- Service 层只依赖 Repository，禁止直接注入 Mapper
- Repository 层封装所有数据库操作，对 Service 层屏蔽 Mapper 细节
- 读操作使用 @Transactional(readOnly = true)
- 使用 MapStruct 进行实体与 DTO 的映射
- 使用 Spring Security + JWT 实现认证授权
- 非阻塞操作使用 @Async
- 启用 Actuator 端点进行应用监控
