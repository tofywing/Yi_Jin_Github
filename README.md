# Yi Jin's Github 🚀 (Coding Test Project)

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
- [x] **编程语言与 API 级别**：使用 **Kotlin** 开发，支持 **API Level 29+**。
- [x] **声明式 UI (Jetpack Compose)**：100% 使用 Compose 构建界面，并深度集成 ViewModel、Coroutine、Flow、Navigation 等架构组件。

---

## 🌟 核心功能

- **探索热门内容**：支持匿名浏览热门仓库和趋势话题。
- **应用内详情预览**：内置安全、流畅的 WebView，无需跳转浏览器即可查看仓库详情。
- **精准搜索与排序**：支持按编程语言搜索仓库，并严格按星标数量进行排序。
- **认证与状态保持**：支持 GitHub PAT 登录，认证状态持久化，登录后直达个人仓库列表。
- **Issue 管理**：认证用户可直接在应用内为其名下的仓库创建 Issue。
- **全方位适配**：完美支持横屏与竖屏模式。
- **健壮性保障**：具备完善的错误处理机制（如断网提醒）。
- **流畅性体验**：集成 Baseline Profile，实现极致的冷启动速度与无卡顿滑动。

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
- **导航组件**：Jetpack Navigation Compose 实现单 Activity 多屏切换。

## 📂 项目结构

```text
├── app/
│   ├── src/main/java/com/example/yijinsgithub/
│   │   ├── common/         # 常量与通用工具
│   │   ├── data/           # 数据层 (Repository, Local, Remote)
│   │   ├── security/       # 加密管理 (Keystore, AES)
│   │   └── ui/             # UI 层 (Screens, Components, ViewModel, Theme)
│   └── src/test/           # 单元测试与 Robolectric UI 测试
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
4. **APK 下载**: 请在项目根目录下的 `release/` 文件夹（或 Release 页面）下载最新的稳定版 APK。

### 3. 运行测试
```bash
# 执行单元测试
./gradlew :app:testDebugUnitTest

# 生成 Jacoco 覆盖率报告 (路径: app/build/reports/jacoco/jacocoTestReport/html/index.html)
./gradlew :app:jacocoTestReport
```

## 🔐 安全与隐私说明

本应用通过 **AES/GCM** 算法对用户的 GitHub Token 进行加密。密钥托管在 Android 系统级的 **Keystore** 中，即使在 Root 环境下也难以被窃取，充分保护用户的隐私。

---
*Created with ❤️ by Yi Jin*
