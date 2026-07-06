# BodyControl 身体训练（安卓）

一款极简的身体训练安卓应用，Kotlin + Jetpack Compose (Material 3) 实现。

## 功能

四大分类，音乐已按分类归入对应条目：

| 分类 | 条目 | 音频 |
| --- | --- | --- |
| 瑜伽 | 拜日式 108 遍、Epic Wahe Guru | ✅ |
| 气功 | 站桩、八段锦、八部金刚功 | ✅ |
| 呼吸法 | 腹式呼吸、4-7-8、箱式呼吸 | 引导（暂无音频） |
| 动作库 | 颈肩放松、脊柱灵活、核心稳定、下肢拉伸 | 引导（暂无音频） |

- 分类网格 → 条目列表 → 点击播放，底部常驻迷你播放器（播放/暂停/停止）。
- 音频文件位于 `app/src/main/res/raw/`。

## 构建（GitHub Actions）

推送到 `main` 或手动触发 `Android CI` 工作流即可构建，产物 `BodyControl-debug-apk` 可在 Actions 运行页下载。

- 工作流：[`.github/workflows/android.yml`](.github/workflows/android.yml)
- 不依赖本地环境，也不需要提交 Gradle wrapper 二进制（CI 使用 `gradle/actions/setup-gradle` 指定的 Gradle 版本）。

## 技术栈

- Kotlin 1.9.24 / AGP 8.5.2 / Gradle 8.9
- compileSdk 34，minSdk 26，targetSdk 34
- Jetpack Compose (BOM 2024.06)，Material 3，MediaPlayer 播放

## 新增音乐

将 mp3 放入 `app/src/main/res/raw/`（文件名仅限小写字母、数字、下划线），再到
[`Catalog.kt`](app/src/main/java/com/bodycontrol/data/Catalog.kt) 中对应分类下新增一条
`TrackItem(rawResId = R.raw.你的文件名)`。
