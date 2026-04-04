# Yi Jin's Github 🚀

**Yi Jin's Github** 是一款基于现代 Android 开发技术栈构建的 GitHub 客户端。该项目展示了如何利用 Jetpack Compose、协程、Flow 以及模块化架构来构建一个高性能、可测试的 Android 应用。

## 🌟 功能特性

- **探索热门仓库**：实时查看 GitHub 上星标数量超过 10,000 的热门项目。
- **灵活搜索**：支持通过关键词和编程语言筛选 GitHub 仓库。
- **身份认证**：支持使用 GitHub 个人访问令牌 (PAT) 进行登录。
- **个人中心**：查看个人资料、统计数据（关注者、仓库数等）以及名下仓库。
- **创建 Issue**：直接在应用内为您的仓库提交新的 Issue。
- **详情展示**：内置安全、流畅的 WebView 容器，支持带身份验证的仓库页面预览。

## 🛠 技术栈

- **UI 框架**：[Jetpack Compose](https://developer.android.com/jetpack/compose) (100% 声明式 UI)
- **架构模式**：MVVM (ViewModel, Repository, DataSources)
- **异步处理**：Kotlin Coroutines & Flow
- **网络层**：[Retrofit](https://square.github.io/retrofit/) + OkHttp + [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **依赖注入**：通过构造函数注入实现轻量级 DI
- **持久化与安全**：
    - [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)：存储用户配置。
    - [AndroidX Security Crypto](https://developer.android.com/jetpack/androidx/releases/security)：对 GitHub PAT 进行加密存储，确保敏感信息安全。
- **图片加载**：[Coil](https://coil-kt.github.io/coil/)
- **性能优化**：包含 **Baseline Profile** 模块，显著提升应用冷启动速度。

## 🧪 测试与质量保证

项目拥有完善的自动化测试体系，确保代码的健壮性和交互的正确性：

- **单元测试 (Unit Tests)**：
    - 使用 **Mockito** 和 **Kotlinx Coroutines Test** 覆盖了 Repository 和 ViewModel 的业务逻辑。
- **界面测试 (UI Tests)**：
    - 结合 **Robolectric** 和 **Compose Test Rule**，在 JVM 环境下快速运行界面交互测试。
- **自动化套件**：通过 `AllUnitTestSuite` 一键运行所有核心测试。
- **代码覆盖率**：集成 **Jacoco** 插件，支持生成详细的测试覆盖率报告。

### 运行测试

```bash
# 运行所有单元测试
./gradlew :app:testDebugUnitTest

# 生成 Jacoco 覆盖率报告
./gradlew :app:jacocoTestReport
```

## 📂 项目结构

```text
├── app/
│   ├── src/main/java/com/example/yijinsgithub/
│   │   ├── common/         # 常量与通用工具
│   │   ├── data/           # 数据层 (Repository, Local, Remote)
│   │   ├── security/       # 加密管理
│   │   └── ui/             # UI 层 (Screens, Components, ViewModel, Theme)
│   └── src/test/           # 单元测试与 Robolectric UI 测试
├── baselineprofile/        # 基准配置文件生成模块
└── gradle/                 # 依赖管理 (Version Catalog)
```

## 🚀 快速开始

1. **生成 GitHub PAT**：
   - 前往 GitHub 设置生成一个 [Personal Access Token (Classic)](https://github.com/settings/tokens)。
   - 权限建议勾选 `repo`。
2. **编译运行**：
   - 使用 Android Studio Ladybug 或更高版本打开项目。
   - 直接运行 `app` 模块。

## 🔐 安全说明

本应用非常重视您的数据安全。所有的个人访问令牌 (PAT) 都通过 Android 系统级密钥库 (Keystore) 加密后存储，确保即使设备环境不安全，敏感信息也难以被泄取。

---
*Created with ❤️ by Yi Jin*
