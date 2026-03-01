# 烁学 (Zhzgo Study) - Android 移动学习平台

![Banner](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue)
![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-orange)
![License](https://img.shields.io/badge/License-GPL_v3-red)

**烁学 (Zhzgo Study)** 是一款专为考生设计的全方位学习与效率矩阵工具。它结合了在线题库、智能解析、多媒体处理工具集，旨在通过端侧算力为用户提供高效、私密的备考体验。

---

## 🌟 核心模块

### 1. 智能备考中心

- **在线题库**：对接 Node.js 后端，支持多科目、海量试题同步（湖北计算机等专业）。
- **多样化题型**：全面支持单选、多选、判断、填空、简答及排序题。
- **✨ AI 智能讲解**：集成 AI (DeepSeek) 深度解析，针对错题提供针对性补强建议。
- **学情分析**：记录答题历史，生成多维度的学习报告与统计仪表盘。

### 2. 实用工具矩阵 (Utility Matrix)

- **多媒体处理**：基于 FFmpeg (Full GPL) 实现高压缩率视频压缩、MP3 音频提取与格式转换。
- **图像引擎**：支持图片缩放、格式转换、滤镜处理及基于 ML Kit 的智能背景抠图。
- **文档助手**：PDF 与图像互转、Markdown 渲染与预览。
- **生活/开发效率**：进制转换、进制计算、亲戚关系计算、色码提取等。

---

## 🛠️ 技术栈

- **语言**：Kotlin (Coroutines + Flow)
- **UI 框架**：Jetpack Compose (Material 3)
- **网络**：Retrofit 2 + OkHttp 4
- **本地存储**：Room Database + DataStore
- **多媒体**：FFmpeg Kit (Full GPL) + MediaStore API (适配 Android 11+ 分区存储)
- **人工智能**：ML Kit (Subject Segmentation)
- **构建工具**：Gradle (KTS) + R8 混淆优化

---

## 🚀 最近更新与优化

- **UI 紧致化**：消除了全局冗余的状态栏边距，界面视觉层级更加清晰。
- **存储架构升级**：完全适配 Android 11-15 的分区存储 (Scoped Storage) 规范，通过 `MediaUtils` 实现媒体库文件即时发现。
- **多媒体能力补完**：升级至 FFmpeg Full GPL 版本，支持 H.264 视频压缩与 LAME MP3 编码。
- **现代化组件**：全量迁移至 Material 3 `AutoMirrored` 矢量图标库，提升 RTL 支持能力。

---

## 📦 快速开始

### 环境要求

- Android Studio Ladybug 或更高版本。
- JDK 17+。
- Android SDK 24 (Min) / 36 (Target)。

### 编译

```bash
./gradlew clean assembleDebug
```

---

## 📄 许可证

本项目核心遵循 **GPL-3.0 License**（基于 FFmpeg Full GPL 授权要求）。

---

## 🤝 贡献与反馈

如果您有任何建议、漏洞反馈或有意向参与贡献，请联系开发者或提交 Issue。
