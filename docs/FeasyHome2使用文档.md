# FeasyHome2 使用文档

> FeasyHome2 = **FeasyHome 蓝牙 Mesh 智能家居 App** 的应用层源码 + 自研 Mesh SDK（AAR）+ 工具库源码。
> 本文档面向接入方（客户/合作方），说明工程结构、编译方式、Mesh SDK 集成与主要 API 用法。

---

## 1. 项目简介

| 项 | 值 |
|---|---|
| App 名称 | FeasyHome |
| applicationId | `com.feasycom.feasyhome` |
| App 版本 | **2.3.6.1**（versionCode 115） |
| Mesh SDK 版本 | **2.0.0.5** |
| 支持系统 | Android 10 及以上（minSdk 29） |
| 目标版本 | compileSdk / targetSdk 36 |
| 功能范围 | 家庭/房间/成员管理、设备入网（灯/窗帘/面板/语音盒子）、分组控制、场景、二维码分享、统计 |

仓库中**不包含 mesh 模块的源码**，该模块以 AAR 形式提供：
`app/libs/feasyhome-mesh-release.aar`。

---

## 2. 环境要求

| 项 | 要求 |
|---|---|
| Android Studio | 需内置 **JDK 17**（AGP 8.x 要求） |
| Android SDK | **API 36**（build-tools 与 platform 都要装） |
| Gradle | 8.13（仓库自带 wrapper，无需单独安装） |
| AGP | 8.13.2（随仓库配置） |
| Kotlin | 1.8.0 |
| Java 编译级别 | 11 |
| Gradle JDK | 17 |

配置 SDK 路径（**必须做**，仓库不包含 `local.properties`）：

- 方式 A：在项目根目录新建 `local.properties`：
  ```properties
  sdk.dir=D\:\\adSdk
  ```
- 方式 B：设置环境变量 `ANDROID_HOME`（或 `ANDROID_SDK_ROOT`）指向 SDK 目录。

---

## 3. 模块说明

| 模块 | 说明 | 仓库中的形式 |
|---|---|---|
| `app` | 应用层：UI、业务逻辑、家庭/房间/场景/设备管理 | **源码** |
| `utilcode` | AndroidUtilCode 工具库（`com.blankj.utilcode`） | **源码** |
| `mesh` | FeasyHome2 Mesh SDK（基于 Nordic Mesh 协议栈扩展，`no.nordicsemi.android.mesh`） | **仅 AAR**（`app/libs/feasyhome-mesh-release.aar`） |

> `settings.gradle` 中只声明了 `:app` 与 `:utilcode`；mesh 通过本地 AAR 引入。

---

## 4. 集成 Mesh SDK（AAR）

### 4.1 放入 AAR

```
app/
└── libs/
    └── feasyhome-mesh-release.aar     ← 由 Feasycom 提供
```

AAR 的 SHA-256（请核对，防止被替换）：

```
9823A548EBCE6022AAC9AC3EDE67F0FE1D50A5CA8496D3F2DA7D29052D6C029B
```

### 4.2 Gradle 依赖

AAR 是**本地文件依赖**，不会自动携带它自身的传递依赖，因此需要在 `app/build.gradle` 中把 mesh 的依赖一并声明：

```groovy
dependencies {
    implementation project(':utilcode')

    // mesh SDK（本地 AAR）
    implementation files('libs/feasyhome-mesh-release.aar')

    // —— mesh 的传递依赖，必须显式声明 ——
    implementation 'androidx.annotation:annotation:1.6.0'
    implementation 'com.madgag.spongycastle:core:1.58.0.0'
    implementation 'com.madgag.spongycastle:prov:1.58.0.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'androidx.room:room-runtime:2.5.1'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
    implementation 'no.nordicsemi.android.support.v18:scanner:1.6.0'
    implementation 'no.nordicsemi.android:ble:2.6.1'
    implementation 'no.nordicsemi.android:log:2.5.0'
}
```

> 若将来改用 Maven 私服，把 AAR 发布成 `no.nordicsemi.android.mesh:mesh:<version>`，
> 上面这些传递依赖就可以省掉。

### 4.3 混淆

AAR 自带 `consumerProguardFiles`（`mesh-proguard-rules.pro`），接入方**无需**再手写 mesh 的 keep 规则；
若开启 `minifyEnabled true`，注意同时保留 Room 实体与 Gson 模型（AAR 已内含规则）。

---

## 5. 初始化

SDK 入口是单例：`no.nordicsemi.android.mesh.fsdk.FscMeshSdk.getApi2()`。

```java
public class HomeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 SDK（必须在使用任何 API 之前调用，建议放在 Application）
        FscMeshSdk.getApi2().Init(this);
        // SDK 版本，例如 "2.0.0.5"
        String sdkVer = FscMeshSdk.getApi2().getSdkVer();
    }
}
```

`AndroidManifest.xml` 中注册：

```xml
<application android:name=".HomeApp" ... />
```

---

## 6. Mesh 网络管理

Mesh 网络（`MeshNetwork`）是设备的“容器”，一个 App 可管理多个网络，当前只有一个处于激活状态。

### 6.1 创建 / 查询 / 切换

```java
Api2 api = FscMeshSdk.getApi2();

// 创建网络
api.CreateMeshNetwork("MyHome", new FscMeshNetworkCallback() {
    @Override public void onSuccess(MeshNetwork network) { /* 创建成功 */ }
    @Override public void onFail(String msg) { /* 失败 */ }
});

// 查询单个网络（按网络 uuid） / 查询全部网络
api.QueryMeshNetwork(meshUuid, fscMeshNetworkCallback);
api.QueryMeshNetworks(new FscMeshNetworksCallback() {
    @Override public void onSuccess(List<MeshNetwork> networks) { }
    @Override public void onFail(String msg) { }
});

// 切换当前网络
api.SwitchMeshNetwork(meshNetwork);
MeshNetwork current = api.getMeshNetwork();

// 删除网络
api.deleteMeshNetworkFromDb(meshNetwork);
```

### 6.2 导出 / 导入（用于备份或跨端迁移）

```java
String json = api.exportMeshNetwork();           // 导出当前网络
String json2 = api.exportMeshNetwork(network);   // 导出指定网络
api.importMeshNetworkJson(json);                 // 导入
api.loadMeshNetwork();                           // 重新加载
```

> 导出内容包含网络密钥等敏感信息，**不要明文存公共目录**。

---

## 7. 扫描与设备入网（Provisioning）

### 7.1 扫描未入网设备

```java
api.StartScan(
    20000,              // stopTime：扫描时长（毫秒）
    filterUuid,         // filterUuid：Mesh 服务 UUID 过滤（一般传 Mesh 的 provision UUID）
    (byte) 0x01,        // mdevPid：设备 PID 标识（0 表示不过滤）
    (byte) 0x00,        // mdevSecondType：设备二级型号（0 表示不过滤）
    new FscScancallback() {
        @Override public void onScanResult(ExtendedBluetoothDevice device) {
            // 每扫到一个设备回调一次：device 里有 MAC / RSSI / 广播数据
        }
        @Override public void onStop() { }
    });

api.StopScan();          // 提前停止

// 让未入网设备闪灯，便于辨认（按 MAC 指定）
api.OpenSearchAdv("AA:BB:CC:DD:EE:FF");
// 未入网设备指示灯开/关（广播方式，全组）
api.OpenIndicatorByAdv(true);
api.CloseAdv();
```

### 7.2 入网

```java
api.StartProvisionedNode(extendedBluetoothDevice, new ProvisioningCallback() {
    @Override public void onProvisioningStart() { }        // 开始入网
    @Override public void onProvisioningProgress(int step, int total) { }  // 进度
    @Override public void onProvisioningSuccess(ProvisionedMeshNode node) {
        // 入网成功，node.getUnicastAddress() 即分配的单播地址
    }
    @Override public void onProvisioningFailed(String reason) { }
});
```

> 入网依赖手机处于**代理（Proxy）连接**状态：SDK 内部会先连 GATT 再走 Provisioning 流程。
> 若设备已入网，需要先用 `ResetNode(unicastAddress)` 或 `ResetAllNode()` 复位后才能重新入网。

---

## 8. 设备控制

Mesh 里所有控制都是**按组地址（group address）**下发；一个节点可以订阅多个组。

### 8.1 节点加入分组

```java
api.AddNodeToGroup(
    meshNode,                 // ProvisionedMeshNode
    groupAddressBytes,        // 组地址（2 字节，如 {0xC0, 0x01}）
    (byte) 1,                 // isGroupleader：1=组主控（BLT_A），0=普通节点
    new SubscribeGroupCallback() {
        @Override public void onSuccess() { }
        @Override public void onFail(String msg) { }
    });
```

### 8.2 灯光

```java
api.ControlLightALL(groupAddr, lightness, ct);   // 同时设置亮度 + 色温
api.ControlLightness(groupAddr, lightness);      // 只设亮度（0~100）
api.ControlLightct(groupAddr, ct);               // 只设色温
api.ControlDevOnOff(groupAddr, (byte) 1);        // 开=1 / 关=0
```

### 8.3 窗帘

```java
api.ControlCurtainOpenPercent(groupAddr, openPercent);  // 开合百分比
api.ControlCurtainDirection(groupAddr, direction);      // 方向（开/关/停）
api.ControlCurtainCleanMil(groupAddr);                  // 行程清零/校准
```

### 8.4 面板（按键）

```java
api.SetPanelBtnLight(unicastAddress, lightness);                 // 按键背光
api.AT_GroupBindPanelBtn(unicastAddress, groupAddr, btnId, doType); // 按键绑定组
api.AT_GroupBindColorBtn(unicastAddress, btnId, doType);            // 按键绑定调色
api.AT_SceneBindPanelBen(unicastAddress, sceneId, btnId, doType);   // 按键绑定场景
```

### 8.5 场景

```java
api.SceneBindLightGroup(sceneId, groupAddr, lightness, ct);   // 场景内绑定灯组参数
api.SceneBindCurtainGroup(sceneId, groupAddr, openPercent);   // 场景内绑定窗帘组
api.SceneUnbindGroup(sceneId, groupAddr);                     // 解绑
api.ControlSceneOnOff(sceneId, (byte) 1);                     // 执行/取消场景
api.DelScene(sceneId);                                        // 删除场景
```

### 8.6 语音盒子 / 房间

```java
api.SyncVoice(unicastAddress, data);        // 下发同步数据给语音盒子
api.SetAssignRoom(unicastAddress, roomId);  // 节点分配到房间
api.SynconeB(unicastAddress, roomCode, iv); // 单播同步（内部协议）
```

---

## 9. 节点查询与复位

```java
ProvisionedMeshNode node = api.getNode(unicastAddress);   // 按单播地址查
api.getNodes(meshUuid, new QuerNodesCallback() {          // 查某个网络的全部节点
    @Override public void onSuccess(List<ProvisionedMeshNode> nodes) { }
    @Override public void onFail(String msg) { }
});
api.deleteNode(node);                                     // 从数据库删除（不移除设备）
api.ResetNode(unicastAddress);                            // 复位指定节点（退回未入网）
api.ResetNodeAT(unicastAddress);                          // 复位（AT 通道变体）
api.ResetAllNode();                                       // 复位所有节点
api.ResetAllNode2();                                      // 复位所有节点（变体）
```

---

## 10. 连接与状态

```java
api.ConnectNetWork(networkId, new FscConnectCallback() {
    @Override public void onConnected() { }
    @Override public void onDisconnected() { }
    @Override public void onConnectFailed(String msg) { }
});

api.isConnect();          // 是否已连上 Proxy
api.getConnectedAdr();    // 当前连接的节点地址
api.Disconnect();         // 断开
api.setClearCacheRequired();  // 下次连接前清 GATT 缓存（换设备/改参数后用）
```

底层 BLE 管理（`BleMeshManager`，一般不需要直接调用）：

| 方法 | 说明 |
|---|---|
| `sendPdu(byte[] pdu)` | 直接下发 PDU（SDK 内部使用） |
| `getMaximumPacketSize()` | 当前 MTU 可发送的最大字节数 |
| `isProvisioningComplete()` | 入网是否完成 |
| `isDeviceReady()` | 设备是否就绪 |

---

## 11. 权限

`app/src/main/AndroidManifest.xml` 已声明的关键权限：

| 权限 | 用途 |
|---|---|
| `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `BLUETOOTH_ADVERTISE` | Android 12+ BLE 扫描/连接/广播 |
| `BLUETOOTH` / `BLUETOOTH_ADMIN` | Android 11 及以下的 BLE 操作 |
| `ACCESS_FINE_LOCATION` | BLE 扫描（Android 11 及以下必需） |
| `INTERNET` / `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE` | 账号、家庭、统计等网络请求 |
| `FOREGROUND_SERVICE` | 后台保持 Mesh 连接 |
| `CAMERA` | 扫二维码加入家庭 |
| `READ/WRITE_EXTERNAL_STORAGE`、`MANAGE_EXTERNAL_STORAGE` | 导出网络 JSON、日志 |

运行时需要动态申请 BLE 与定位权限（工程内使用 **XXPermissions** 处理）。

---

## 12. 编译与签名

```bash
# Debug
./gradlew :app:assembleDebug

# Release（需要自己配置签名）
./gradlew :app:assembleRelease
```

**签名**：仓库中 `app/build.gradle` 的 `signingConfigs` 已被移除（keystore 与密码属于内部资源）。
需要签名包时，请在自己机器上添加：

```groovy
android {
    signingConfigs {
        release {
            storeFile file("your.jks")
            storePassword "..."
            keyAlias "..."
            keyPassword "..."
        }
    }
    buildTypes {
        release { signingConfig signingConfigs.release }
    }
}
```

⚠️ 不要把 keystore 与密码提交到公开仓库（`.gitignore` 已忽略 `*.jks` / `*.keystore`）。

---

## 13. 常见问题

| 现象 | 排查 |
|---|---|
| `SDK location not found` | 配 `local.properties` 的 `sdk.dir` 或 `ANDROID_HOME` |
| 找不到 `no.nordicsemi.android.mesh.*` 类 | 确认 `app/libs/feasyhome-mesh-release.aar` 存在，且 `implementation files(...)` 已配置 |
| 找不到 Room/Gson/Nordic BLE 类 | 4.2 中的传递依赖没补全 |
| 扫描不到设备 | 蓝牙开关、定位开关、BLE 权限、是否已被其他手机连接 |
| 入网一直失败 | 设备可能已在别的网络里 → `ResetNode` 后重试；靠近设备；确认 PID 过滤参数 |
| 控制无响应 | 检查 `isConnect()`；组地址是否正确；节点是否已订阅该组 |
| 多台手机控制不同步 | 使用 SDK 2.0.0.4+ 的网络导出/导入与 `SwitchMeshNetwork` 保持同一网络 |

---

## 14. SDK 版本记录

| 版本 | 内容 |
|---|---|
| 2.0.0.5 | 增加统计接口 |
| 2.0.0.4 | 解决多端手机控制不了的问题并修复已知 BUG |
| 2.0.0.3 | 修复部分已知 BUG |
| 2.0.0.2 | 新增面板和语音盒子入网 |
| 2.0.0.1 | 新增灯光和窗帘入网 |
| 2.0.0.0 | 首次发布 |

---

## 15. 附：关键类速查

| 类 | 包 | 说明 |
|---|---|---|
| `FscMeshSdk` | `no.nordicsemi.android.mesh.fsdk` | SDK 入口，`getApi2()` |
| `Api2` | `no.nordicsemi.android.mesh.fsdk` | 高层接口（本文档第 6~10 节的方法都在这里） |
| `MeshManagerApi` | `no.nordicsemi.android.mesh` | 底层 Mesh 协议栈管理（入网、PDU、网络导出等） |
| `MeshMngrApi` | `no.nordicsemi.android.mesh` | `MeshManagerApi` 的接口定义 |
| `BleMeshManager` | `no.nordicsemi.android.mesh.fsdk` | GATT 连接与 PDU 收发 |
| `ProvisionedMeshNode` | `no.nordicsemi.android.mesh.transport` | 已入网节点模型 |
| `UnprovisionedMeshNode` | `no.nordicsemi.android.mesh.provisionerstates` | 未入网节点模型 |
| `ExtendedBluetoothDevice` | `no.nordicsemi.android.mesh.fsdk` | 扫描结果（含 MAC/RSSI） |
