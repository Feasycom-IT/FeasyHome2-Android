# FeasyHome2（FeasyHome App + Mesh SDK）

FeasyHome2 是 **FeasyHome 蓝牙 Mesh 智能家居 App** 的工程：

- `app`：应用层源码（家庭/房间/场景/设备入网与控制）
- `utilcode`：AndroidUtilCode 工具库源码（`com.blankj.utilcode`）
- `mesh`：**FeasyHome2 Mesh SDK（基于 Nordic Mesh 协议栈扩展）**，本仓库**不包含其源码**，以 AAR 形式提供：
  `app/libs/feasyhome-mesh-release.aar`

> Application-layer source of the **FeasyHome** Bluetooth Mesh smart-home app.
> The proprietary **Mesh SDK** is shipped as a prebuilt AAR (`app/libs/feasyhome-mesh-release.aar`);
> its source is **not** included in this repository.

---

## 版本 / Version

| 项 | 值 |
|---|---|
| App | **2.3.6.1**（versionCode 115） |
| Mesh SDK | **2.0.0.5** |
| applicationId | `com.feasycom.feasyhome` |
| minSdk / compileSdk / targetSdk | 29 / 36 / 36 |
| AGP / Gradle / Kotlin | 8.13.2 / 8.13 / 1.8.0 |

---

## 下载安装包 / Download APK

| 版本 | 文件 | 大小 | SHA-256 |
|---|---|---|---|
| v2.3.6.1 | [apk/FeasyHome2-v2.3.6.1.apk](apk/FeasyHome2-v2.3.6.1.apk) | 20.08 MB | `8DF4A871B170B3F9E1F46318FD7EE5AA9B7C9DC22315F8788F5E2BFC7EB38189` |

- 由**官方签名证书**签名（非调试包），可直接安装 / 覆盖升级；
- 适用系统：**Android 10 及以上**（minSdk 29）；
- 与仓库源码版本 **2.3.6.1** 一致。
## 快速开始 / Quick start

```bash
# 1) 配置 SDK 路径（仓库不含 local.properties）
echo sdk.dir=D:\\adSdk > local.properties      # 或设置 ANDROID_HOME

# 2) 编译
./gradlew :app:assembleDebug
```

环境要求：**Android Studio + JDK 17**、Android SDK **API 36**。
Requirements: Android Studio with **JDK 17**, Android SDK **API 36**.

---

## 模块 / Modules

| 模块 | 说明 | 形式 |
|---|---|---|
| `app` | 应用层（UI + 业务） | 源码 / source |
| `utilcode` | AndroidUtilCode 工具库 | 源码 / source |
| `mesh` | Mesh SDK（`no.nordicsemi.android.mesh`） | AAR |

`mesh` 的 AAR 校验值：

```
SHA-256: 9823A548EBCE6022AAC9AC3EDE67F0FE1D50A5CA8496D3F2DA7D29052D6C029B
```

> 本地 AAR 不携带传递依赖，`app/build.gradle` 里显式声明了 mesh 的依赖
> （Room / Gson / SpongyCastle / Nordic BLE 等），详见文档第 4 节。

---

## 文档 / Documentation

| 语言 / Language | 文档 |
|---|---|
| 中文 | [docs/FeasyHome2使用文档.md](docs/FeasyHome2使用文档.md) |
| English | [docs/FeasyHome2_Integration_Guide_EN.md](docs/FeasyHome2_Integration_Guide_EN.md) |

文档包含：环境要求、模块说明、Mesh SDK 集成（AAR + 传递依赖）、初始化、
网络管理、扫描与入网、设备/分组/场景控制、节点复位、连接状态、权限、编译签名、常见问题、SDK 版本记录。

---

## 说明 / Notes

- 本仓库只发布**对外资源**：应用层源码 + 工具库源码 + Mesh SDK 的 AAR + 中英文文档。
- **不包含** `mesh` 模块源码，也**不包含**签名密钥（`signingConfigs` 已移除）。
  Signing config has been removed on purpose — configure your own keystore locally.
- 导出网络（`exportMeshNetwork`）的结果含网络密钥，请勿明文存放于公共目录。
