# 🚀 项目上下文简报：Yi Jin's Github (Android)

#### 1. 项目概览
*   **目标**：一个基于 GitHub API 的仓库浏览与管理应用。支持热门仓库查看、搜索、用户登录、个人信息展示及 Issue 创建。
*   **核心架构**：MVVM + Clean Architecture 简化版。使用 `GithubViewModel` 管理状态，`GithubRepository` 处理数据。

#### 2. 技术栈 (Tech Stack)
*   **UI**: Jetpack Compose (Material 3), Navigation Compose, Coil (图片加载)。
*   **网络**: Retrofit + Kotlinx Serialization, OkHttp (带 AuthInterceptor 注入 Token)。
*   **持久化/安全**: DataStore (存储 Token), AES-GCM 加密 (`CryptoManager`), RootBeer (脱壳/真机环境检测), APK 签名校验。
*   **测试**: JUnit 4, Mockito-Kotlin, Robolectric (用于处理 Android 框架依赖), Compose UI Test。

#### 3. 核心 API 变更记录 (⚠️ 重要)
*   **分页支持**：`HomeScreen` 和 `SearchScreen` 已升级支持分页。
    *   **必需参数**：调用时必须传入 `isLoadingMore: Boolean`, `isLastPage: Boolean`, `onLoadMore: () -> Unit`, 以及 `listState: LazyListState`。
*   **Repository 签名**：`searchRepositories` 和 `getPopularRepositories` 现在接受 `page` 和 `perPage` 参数。

#### 4. 排序逻辑 (Sorting Logic)
*   **HomeScreen (首页)**:
    *   **未登录**: 搜索 `stars:>10000` 的仓库，按 **Stars** 降序排列。
    *   **已登录**: 获取当前用户的仓库 (`/user/repos`)。*注意：默认按时间（最近推送）排序，时间相同的按 `full_name` 排序*。
*   **SearchScreen (搜索)**:
    *   固定按 **Stars** 降序排列（由 `Constants.DEFAULT_SORT` 定义）。

#### 5. 测试环境约束
*   **加解密测试**：`CryptoManagerTest` 必须运行在 **Robolectric** 环境下（使用 `@RunWith(RobolectricTestRunner::class)`），否则会因为 `android.util.Base64` 未 mock 而报错。
*   **ViewModel 测试**：使用 Mockito 编写 `whenever` 时，若方法有多个参数，必须**全部使用 Matchers** (如 `any()`, `anyOrNull()`, `anyInt()`)，不能将 Matcher 与原生字面量混合使用。

#### 6. 项目约束与规范
*   **权限**：`INTERNET` 权限是必需的。
*   **Git 分支策略**：当前特定分支（如 `home_assignment`）实行严格的过滤政策。`.gitignore` 配置为**仅允许**上传 `yijin_github.apk` 和 `README.md`。
*   **代码风格**：现代 Android 开发规范，优先使用 Compose 和协程。
