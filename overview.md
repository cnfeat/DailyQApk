# 交付报告：日课一问 App 升级

**TL;DR**：从 12 条硬编码 Widget 升级为 401 题完整 App + Widget，包名统一为 `com.dailyquestion`。

## 升级概览

| 项目 | 旧版本 | 新版本 |
|------|--------|--------|
| 包名 | `com.rikeyiwen` | `com.dailyquestion` |
| 问题库 | 12 条硬编码 | 401 题（来自 data.json） |
| App 首页 | 简单 TextView | 卡片式布局 + 深度追问 |
| 换题 | 无限次随机 | 每日最多 3 次 |
| Widget | 12 条随机 | 401 题库 + 限次换题 |
| 依赖 | 无 Gson | 新增 Gson JSON 解析 |

## 文件清单

### 新增文件（6 个）
- `app/src/main/res/raw/questions.json` — 401 题完整问题库
- `.../model/Question.kt` — 数据模型
- `.../model/QuestionManager.kt` — 每日状态管理器
- `app/src/main/res/drawable/card_background.xml` — 卡片圆角背景
- `app/src/main/res/drawable/button_primary.xml` — 按钮蓝色背景
- `app/src/main/res/drawable/button_disabled.xml` — 按钮灰色禁用
- `app/src/main/res/values/colors.xml` — 颜色定义

### 重写文件（9 个）
- `app/build.gradle.kts` — 改包名 + 加 Gson
- `AndroidManifest.xml` — 改主题引用
- `MainActivity.kt` — 全新卡片交互
- `DailyQuestionData.kt` — 桥接 QuestionManager
- `DailyQuestionWidget.kt` — 升级 401 题库 + 限次
- `DailyQuestionWorker.kt` — 定时刷新用新数据源
- `DailyQuestionWidgetReceiver.kt` — 改包名
- `activity_main.xml` — 全新卡片布局
- `themes.xml` — 改为 DailyQuestion 主题
- `values-night/themes.xml` — 深色主题同步
- `strings.xml` — 全面更新文案
- `settings.gradle.kts` — 项目名改为 DailyQuestion
- `proguard-rules.pro` — 改包名引用
- `gradle/libs.versions.toml` — 添加 Gson 版本

### 删除文件
- `com/rikeyiwen/` 整个旧包目录已被清理

## App 交互流程

```
打开 App
    ↓
显示今日问题（卡片形式）
    ├─ 点击卡片 → 展开「深度追问」（extension 内容）
    ├─ 点击「换一问」→ 随机换题，剩余次数 -1
    │   └─ 3 次用完 → 按钮变灰「今日已用完」
    └─ 每天 0:00 → 自动重置，新题 + 新次数
```

## 技术要点

- **数据层**：`res/raw/questions.json` → Gson 解析 → `QuestionManager` 单例管理
- **状态持久化**：SharedPreferences 存 `today_date`, `today_question_id`, `switch_count`
- **每日重置**：日期比对，日期变更时自动选新题 + 归零计数
- **Widget**：Glance 状态与 SharedPreferences 协同，点击调用 `switchToNext()`
- **定时任务**：WorkManager 每天 8:00 刷新（与 App 状态无关，仅限时更新 Widget）

## 后续可做

- v2：思考记录 / 日记功能
- v2：卡片海报生成分享
- v3：iOS 端
- v3：云端同步（多设备）
