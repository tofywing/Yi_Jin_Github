# Yi Jin's Github 🚀 (Coding Test Project)

> [!IMPORTANT]
> **关于应用内 WebView 登录的说明**：
> 在应用内通过 WebView 查看仓库详情或进行交互时，用户需要**在 WebView 中再次登录 GitHub 账号**。
>
> **GitHub 限制与安全说明**：
> 1. **令牌用途限制**：用户登录时提供的个人访问令牌 (PAT) 仅用于 GitHub REST API 交互，无法直接用于网页版（WebView）的身份认证。
> 2. **会话隔离**：GitHub 网页版依赖于 Session 和 Cookies 进行安全管理。出于安全合规要求和技术限制，PAT 不应也无法通过 WebView 注入或通过 URL 传递给 GitHub 官网，否则会面临 Token 泄露及 CSRF 攻击风险。
> 3. **最佳实践**：为了保护您的账号安全，本应用严格隔离了 API Token 与 Web 浏览会话。

**Yi Jin's Github** 是一款基于现代 Android 开发技术栈构建的 GitHub 客户端，完全遵循 **GitHub Android Application Coding Test** 的各项要求进行开发。

---

## 📋 需求实现对应表 (Requirement Mapping)

本项目严格对齐面试题中的 **User Requirement** 与 **Technical Requirement**：

### 👤 用户需求 (User Requirements)
- [x] **未登录浏览**：用户无需登录即可探索 GitHub 上的热门仓库（Star > 10,000）。
- [x] **应用内浏览详情**：内置流畅的 WebView 容器，支持在应用内直接查看仓库详情，无需跳转外部浏览器。
- [x] **搜索功能**：支持按编程语言进行筛选搜索，并默认按星标（Stars）数量降序排列。
- [x] **身份认证与状态持久化**：支持使用 GitHub 个人访问令牌 (PAT) 登录。登录后直达个人仓库列表，且认证状态在应用重启后依然保持。
- [x] **创建 Issue**：认证用户可以为其名下的仓库直接在应用内创建新的 Issue。
- [x] **退出登录**：支持一键注销，返回匿名浏览状态。
- [x] **横竖屏支持**：UI 适配所有屏幕方向，支持 Portrait 和 Landscape 模式。
- [x] **错误处理**：具备完善的异常捕捉机制，如网络连接中断时的友好提示与处理。

### 🛠 技术需求 (Technical Requirements)
- [x] **代码托管与介绍**：代码托管于 GitHub，提供详细的构建、运行及测试指南。
- [x] **GitHub REST API**：完全采用 GitHub 官方 REST API 进行数据交互。
- [x] **编程语言与 API 级别**：使用 **Kotlin (2.0+)** 开发，支持 **API Level 29+**。
- [x] **声明式 UI (Jetpack Compose)**：100% 使用 Compose 构建界面，并深度集成 ViewModel、Coroutine、Flow、Navigation 等架构组件。

---

## 🌟 核心功能

- **探索热门内容**：支持匿名浏览热门仓库和趋势话题。
- **应用内详情预览**：内置安全、流畅的 WebView，无需跳转浏览器即可查看仓库详情。
- **精准搜索与排序**：支持按编程语言搜索仓库，并严格按星标数量进行排序。
- **认证与状态保持**：支持 GitHub PAT 登录，认证状态持久化，登录后直达个人仓库列表。
- **Issue 管理**：认证用户可直接在应用内为其名下的仓库创建 Issue。
- **全方位适配**：完美支持横屏与竖屏模式。
- **高性能列表**：`RepoList` 组件经过深度优化，使用 GPU 加速层、稳定 Key 机制以及 `derivedStateOf` 最小化重组。
- **健壮性保障**：具备完善的错误处理机制（如断网提醒）。
- **流畅性体验**：集成 Baseline Profile，实现极致的冷启动速度与无卡顿滑动。
- **多语言支持**：全面支持中英文双语切换。
- **深色模式适配**：完美适配系统深色/浅色主题，支持 Android 12+ 动态配色。

## 🛠 技术栈

- **UI 框架**：[Jetpack Compose](https://developer.android.com/jetpack/compose) (100% 声明式 UI)
- **架构模式**：MVVM (ViewModel, Repository, DataSources)
- **异步处理**：Kotlin Coroutines & Flow
- **网络层**：[Retrofit 2.11.0](https://square.github.io/retrofit/) + OkHttp + Official [Kotlinx Serialization Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/kotlinx-serialization)
- **依赖注入**：通过构造函数注入实现轻量级 DI
- **持久化与安全**：
    - [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)：存储用户配置。
    - [AndroidX Security Crypto](https://developer.android.com/jetpack/androidx/releases/security)：对 GitHub PAT 进行加密存储，确保敏感信息安全。
    - **网络安全配置 (Network Security Config)**：通过 XML 配置声明式地管理网络安全策略。
    - **环境检测 (Integrity Protection)**：集成 [RootBeer](https://github.com/scottyab/rootbeer) 实现运行时 Root 与模拟器检测。
    - **签名校验 (Anti-Tampering)**：运行时校验 APK 签名哈希，防止二次打包攻击。
- **图片加载**：[Coil](https://coil-kt.github.io/coil/)
- **性能优化**：包含 **Baseline Profile** 模块，显著提升应用冷启动速度。
- **导航组件**：Jetpack Navigation Compose 实现单 Activity 多屏切换。

## 📂 项目结构

```text
├── app/
│   ├── src/main/java/com/example/yijinsgithub/
│   │   ├── common/         # 常量与通用工具
│   │   ├── data/           # 数据层 (Repository, Local, Remote)
│   │   ├── security/       # 安全管理 (Keystore, Crypto, Signature, Audit)
│   │   └── ui/             # UI 层 (Screens, Components, ViewModel, Theme)
│   ├── src/test/           # 单元测试 (JUnit, Mockito, Robolectric)
│   └── src/androidTest/    # 工具化测试 (Compose Test Rule)
├── baselineprofile/        # 基准配置文件生成模块 (性能优化)
└── gradle/                 # 依赖管理 (Version Catalog)
```

## 🧪 测试与质量保证

项目覆盖了深度的自动化测试，确保逻辑可靠：
- **单元测试**: Mockito + Coroutines Test (ViewModel/Repository 逻辑覆盖 > 90%)
- **UI 测试**: Robolectric + Compose Test Rule
- **覆盖率报告**: 集成 Jacoco 插件。

---

## 🚀 编译、运行与测试指南

### 1. 环境准备
- **Android Studio**: 建议使用 **Ladybug (2024.2.1)** 或更高版本。
- **JDK**: Java 17。
- **GitHub PAT**: 前往 [GitHub Settings](https://github.com/settings/tokens) 生成一个具有 `repo` 权限的 Classic Token。

### 2. 编译与运行
1. 克隆仓库。
2. 在 Android Studio 中点击 `Sync Project with Gradle Files`。
3. 选择 `app` 模块，点击 `Run`。

### 3. 运行测试
```bash
# 执行单元测试
./gradlew :app:testDebugUnitTest

# 执行 Android UI 测试
./gradlew :app:connectedDebugAndroidTest

# 生成 Jacoco 覆盖率报告 (路径: app/build/reports/jacoco/jacocoTestReport/html/index.html)
./gradlew :app:jacocoTestReport
```

## 🔐 安全与隐私说明

本项目采取了多重安全措施保障用户数据与通信安全，符合金融级应用的安全标准：

1.  **数据加密存储**：通过 **AES/GCM** 算法对用户的 GitHub Token 进行加密。密钥托管在 Android 系统级的 **Keystore** 中，即使在 Root 环境下也难以被窃取。
2.  **通信安全策略**：
    *   **禁止明文传输**：通过 `network_security_config.xml` 强制全局禁用 HTTP 明文流量，仅允许加密的 HTTPS 连接。
    *   **证书校验优化**：采用 Android 官方推荐的 `Network Security Configuration` 机制。相比于代码中硬编码且易过期的 SSL Pinning，该方式能更好地平衡安全性和维护成本。
3.  **运行时完整性保护 (Runtime Integrity)**：
    *   **Root & 模拟器检测**：集成 RootBeer 库。应用启动时自动检测运行环境，若发现设备已 Root 或运行在不安全的模拟器上，将通过 `SecurityRiskScreen` 立即拦截。
    *   **代码防篡改 (Anti-Tampering)**：内置签名哈希校验逻辑，防止应用被二次打包或恶意篡改。
4.  **敏感数据内存安全 (In-Memory Security)**：
    *   **输入脱敏 (Data Masking)**：登录界面的 PAT 输入框采用 `PasswordVisualTransformation`，防止“肩窥”攻击。
    *   **状态隔离**：敏感 Token 仅在受保护的 `AuthInterceptor` 作用域内短暂存在，减少内存被 Dump 的风险。
5.  **审计与异常追踪 (Security Auditing)**：
    *   **事件记录**：应用内置了安全审计机制，能够捕捉并记录运行时发生的完整性违规（如 Root 风险、签名不匹配）。
    *   **可扩展性**：审计日志遵循标准规范，可轻松集成至远程安全监控系统，为应用的安全态势感知提供数据支撑。

---
*Created with ❤️ by Yi Jin*
