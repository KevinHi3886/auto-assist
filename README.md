# Auto Assist

![Auto Assist icon](src/main/resources/assets/autoassist/icon.png)

作者：**Beta1122**

> **GitHub 开源：** [KevinHi3886/auto-assist](https://github.com/KevinHi3886/auto-assist) · MIT License · [问题反馈](https://github.com/KevinHi3886/auto-assist/issues)

一个 **Minecraft 26.1–26.3 / Fabric 客户端** 综合实用模组。按游戏版本提供四个分开的 jar，每个 jar 只加载对应版本，四个版本都已实机测试：

- **自动补充**：当前快捷栏物品用完后，自动从背包补上相同物品。
- **自动进食**：饱食度低于设定阈值（默认 16）时，自动吃背包里最合适的健康食物。
- **自动搭桥+**：按 **V** 开关，在脚下自动放置手中方块。
- **控制面板**：按 **F8** 打开，可开关功能并调整自动进食阈值。

## 功能说明

### 自动补充

- 只在当前快捷栏槽位从“有物品”变成“空”时触发，不会因为手动切到空槽而误补。
- 优先从主背包找相同物品，通过原版容器点击交换到当前槽位。
- 可堆叠物品按“物品 + 数据组件”匹配；有耐久的工具/武器只按物品类型匹配。

### 自动进食

- 饱食度低于控制面板中设置的阈值（默认 **16**）时自动开始进食。
- 优先选择营养值高、无负面效果的食物；会跳过腐肉、河豚、蜘蛛眼、毒马铃薯、紫颂果等。
- 会检查食物携带的状态效果，带有负面效果的食物不会被选中。
- 快捷栏没有合适食物时，会自动从主背包把食物换到快捷栏。
- 进食到饱食度超过设定阈值后停止。

### 自动搭桥+

- 默认按键 **V**，可在“选项 → 控制 → 游戏玩法”里修改。
- 开启后，手持方块走到边缘/脚下为空时自动在脚下放置方块。
- 默认关闭，需要按 V 开启。

## 控制面板

按 **F8** 打开控制面板（可在“选项 → 控制 → 杂项”中修改按键）。

面板内可以：

- 开关自动补充
- 开关自动进食
- 调整自动进食阈值（1–20，默认 16）
- 开关自动搭桥（与 V 键同步）
- 点击 Done 保存并关闭

设置会保存在 `.minecraft/config/autoassist.json`，重启游戏后保留。

## 版本文件

| Minecraft | 文件 | Fabric Loader |
| --- | --- | --- |
| 26.1 | `autoassist-1.2.0-mc26.1.jar` | 0.19.2+ |
| 26.1.2 | `autoassist-1.2.0-mc26.1.2.jar` | 0.19.2+ |
| 26.2 | `autoassist-1.2.0-mc26.2.jar` | 0.19.5+ |
| 26.3 | `autoassist-1.2.0-mc26.3.jar` | 0.19.5+ |

## 安装

1. 安装对应版本的 Fabric Loader。
2. 需要对应版本的 **Fabric API**（26.1、26.1.2、26.2 或 26.3）。
3. 将对应版本的 jar 放入 `.minecraft/mods`，不要同时放多个版本。
4. 仅客户端模组，服务器不需要安装。

> 如果你之前装了单独的 `autorefill-1.0.0.jar` 或 `bridge-mod-1.1.3-mc26.3.jar`，使用本整合版时请移除它们，避免功能重复触发。

## 构建

需要 **JDK 25**。项目包含 Gradle Wrapper：

```bash
./gradlew build
```

Windows：

```bat
gradlew.bat build
```

产物：`build/libs/autoassist-1.2.0.jar`（通用构建）

> [Releases](https://github.com/KevinHi3886/auto-assist/releases) 页面提供四个分版本文件，只想使用的话无需构建。Gradle 产物是通用 jar，四个分版本文件是在其基础上按版本改写 `fabric.mod.json` 生成的。

## 项目结构

```text
src/client/java/com/beta1122/autoassist/
  AutoAssistClient.java              客户端入口与 tick 调度
  AutoAssistConfig.java              配置读写
  ClientCompat.java                  26.1–26.3 客户端兼容层
  gui/AutoAssistConfigScreen.java    游戏内控制面板
  feature/AutoRefill.java            自动补充
  feature/AutoEat.java               自动进食
  feature/AutoBridge.java            自动搭桥+
src/main/resources/
  fabric.mod.json
  pack.mcmeta
  assets/autoassist/icon.png
  assets/autoassist/lang/
```

## 说明

自动搭桥功能基于作者 Beta1122 的 **Bridge Mod+ 1.1.3**（CC0）整理而来；自动补充功能来自 **AutoRefill 1.0.0**。整合版统一使用 MIT 许可证。

## 许可证

MIT
