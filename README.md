# Auto Assist

![Auto Assist icon](src/main/resources/assets/autoassist/icon.png)

> **开源地址：** https://github.com/KevinHi3886/auto-assist  
> 本项目使用 MIT License，欢迎提交 Issue 或 Pull Request。

一个面向 **Minecraft 26.3 / Fabric** 的客户端实用模组，把三个常用功能整合进同一个 jar：

- **自动补充**：快捷栏物品用完后，从背包补上相同物品。
- **自动进食**：饱食度低于设定阈值（默认 16）时，自动吃合适的健康食物。
- **自动搭桥+**：按 **V** 开关，在脚下自动放置手中方块。
- **控制面板**：按 **F8** 打开，可开关功能并调整自动进食阈值。

全部功能都只在客户端运行，服务器不需要安装。

## 功能

### 自动补充

- 当前快捷栏槽位从“有物品”变成“空”时触发，手动切到空槽不会误触发。
- 优先从主背包寻找相同物品，通过原版容器点击交换到当前槽位。
- 可堆叠物品按“物品 + 数据组件”匹配；有耐久的工具/武器只按物品类型匹配。

### 自动进食

- 饱食度低于控制面板中设置的阈值（默认 **16**）时自动开始进食。
- 优先选择营养值高、无负面效果的食物。
- 会跳过腐肉、河豚、蜘蛛眼、毒马铃薯、紫颂果等有害食物，并检查食物携带的状态效果。
- 快捷栏没有合适食物时，会自动从主背包换到快捷栏。
- 饱食度超过设定阈值后停止。

### 自动搭桥+

- 默认按键 **V**，可在“选项 → 控制 → 游戏玩法”中修改。
- 开启后，手持方块走到边缘或脚下为空时，自动在脚下放置方块。
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

## 环境要求

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.3 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 26.3 对应版本 |
| Java | 25 |

## 安装

1. 安装 Fabric Loader 和 Fabric API。
2. 从 [Releases](https://github.com/KevinHi3886/auto-assist/releases) 页面下载最新的 `autoassist-*.jar`。
3. 将 jar 放入 `.minecraft/mods`。
4. 启动游戏。

> 如果之前安装过单独的 AutoRefill、Bridge Mod+ 或功能重复的自动进食/自动补充模组，请先移除，避免重复触发。

## 构建

需要 **JDK 25**。项目包含 Gradle Wrapper：

```bash
./gradlew build
```

Windows：

```bat
gradlew.bat build
```

产物位于 `build/libs/autoassist-1.1.0.jar`。

## 项目结构

```text
src/client/java/com/beta1122/autoassist/
  AutoAssistClient.java              客户端入口与 tick 调度
  AutoAssistConfig.java              配置读写
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

## 致谢

- 自动搭桥功能整理自作者 **Beta1122** 的 **Bridge Mod+ 1.1.3**（CC0）。
- 自动补充功能来自 **AutoRefill 1.0.0**。

## 许可证

[MIT](LICENSE)
