# 日课一问 — Android

每日一问，破局人生。一款帮你建立每日反思习惯的 Android 应用。

> 「日课一问」是一套卡片问题集，内置 400+ 灵魂拷问，效法《论语》「吾日三省吾身」，每日随机 1 问，助你构建起一套自我进化的人生反思系统。

## 功能

- **每日一问** — 每天自动推送一道精选反思问题
- **深度追问** — 每个问题附带拓展引导，层层深入
- **换题机制** — 每日可换题最多 3 次，找到最触动你的问题
- **桌面小部件** — 4×2 Glance Widget，主屏幕直接查看当日问题
- **分享卡片** — 一键生成精美卡片海报，保存或分享
- **深色模式** — 支持明暗主题切换
- **问题库** — 内置 400+ 道深度反思问题，覆盖人生、事业、认知等多维领域

## 截图

| | | |
|:---:|:---:|:---:|
| 主界面 | 桌面小部件 | 分享卡片 |
| *(等待截图)* | *(等待截图)* | *(等待截图)* |

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM（单 Activity） |
| 桌面小部件 | Jetpack Glance |
| 定时更新 | WorkManager |
| 数据存储 | DataStore Preferences + JSON 本地文件 |
| 主题 | Material 3 Dynamic Color / 自定义绿色主题 |
| 最低 SDK | Android 8.0 (API 26) |
| 目标 SDK | Android 15 (API 35) |

## 构建

使用 Android Studio 打开项目根目录的 `DailyQApk/` 文件夹，同步 Gradle 后即可构建。

```bash
# 或命令行构建
cd DailyQApk
./gradlew assembleDebug
```

APK 输出位置：`DailyQApk/app/build/outputs/apk/debug/`

## 项目结构

```
DailyQApk/
├── app/
│   ├── src/main/
│   │   ├── java/com/dailyquestion/
│   │   │   ├── MainActivity.kt          # 入口 Activity
│   │   │   ├── DailyQuestionData.kt     # 数据加载
│   │   │   ├── DailyQuestionWidget.kt   # Glance Widget 实现
│   │   │   ├── DailyQuestionWorker.kt   # WorkManager 定时任务
│   │   │   ├── WidgetUpdater.kt         # Widget 更新管理
│   │   │   ├── model/
│   │   │   │   ├── Question.kt          # 问题数据模型
│   │   │   │   └── QuestionManager.kt   # 问题管理逻辑
│   │   │   └── ui/
│   │   │       ├── screen/
│   │   │       │   ├── MainScreen.kt    # 主界面
│   │   │       │   └── CircularYearProgress.kt
│   │   │       ├── component/
│   │   │       │   ├── QuestionCard.kt  # 问题卡片
│   │   │       │   ├── SettingsSheet.kt # 设置面板
│   │   │       │   └── DotIndicator.kt  # 圆点指示器
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt         # 颜色定义
│   │   │       │   ├── Theme.kt         # 主题配置
│   │   │       │   └── Type.kt          # 排版
│   │   │       └── util/
│   │   │           ├── HapticUtil.kt    # 振动反馈
│   │   │           └── ShareUtil.kt     # 分享卡片生成
│   │   ├── res/
│   │   │   ├── raw/questions.json       # 问题库（400+ 条）
│   │   │   ├── drawable/                # 图标矢量图
│   │   │   ├── values/                  # 颜色、字符串、主题
│   │   │   └── xml/                     # Widget 配置
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew
```

## 问题库

问题数据存储在 `app/src/main/res/raw/questions.json`，每条问题包含：
- `id` — 编号
- `question` — 问题正文
- `extension` — 拓展追问引导

可通过修改此文件增删问题内容，无需修改代码。

## 许可

本项目基于 MIT 协议开源。

## 致谢

- 问题库源自 [DailyQuestion](https://github.com/cnfeat/DailyQuestion) Chrome 浏览器扩展
- 应用图标自行设计
