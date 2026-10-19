# FeasyHome2 Integration Guide

> FeasyHome2 = the application-layer source of the **FeasyHome Bluetooth Mesh smart-home app**,
> plus the proprietary **Mesh SDK (AAR)** and the **utility library source**.
> This guide is written for integrators (customers / partners): project layout, build steps,
> Mesh SDK integration, and the main APIs.

---

## 1. Overview

| Item | Value |
|---|---|
| App name | FeasyHome |
| applicationId | `com.feasycom.feasyhome` |
| App version | **2.3.6.1** (versionCode 115) |
| Mesh SDK version | **2.0.0.5** |
| Supported OS | Android 10+ (minSdk 29) |
| Compile/Target SDK | 36 |
| Scope | Families / rooms / members, device provisioning (lights, curtains, panels, voice boxes), group control, scenes, QR sharing, statistics |

The **mesh module source is not part of this repository**. It is shipped as a prebuilt AAR:
`app/libs/feasyhome-mesh-release.aar`.

---

## 2. Requirements

| Item | Requirement |
|---|---|
| Android Studio | with **JDK 17** (required by AGP 8.x) |
| Android SDK | **API 36** (platform + build-tools) |
| Gradle | 8.13 (wrapper included) |
| AGP | 8.13.2 |
| Kotlin | 1.8.0 |
| Java compile level | 11 |
| Gradle JDK | 17 |

Configure the SDK location (**required** — this repo does not ship `local.properties`):

- Option A — create `local.properties` in the project root:
  ```properties
  sdk.dir=D\:\\adSdk
  ```
- Option B — set the `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) environment variable.

---

## 3. Modules

| Module | Description | Form in this repo |
|---|---|---|
| `app` | Application layer: UI, business logic, family/room/scene/device management | **source** |
| `utilcode` | AndroidUtilCode (`com.blankj.utilcode`) | **source** |
| `mesh` | FeasyHome2 Mesh SDK (built on the Nordic Mesh stack, `no.nordicsemi.android.mesh`) | **AAR only** (`app/libs/feasyhome-mesh-release.aar`) |

> `settings.gradle` only declares `:app` and `:utilcode`; the mesh module is consumed as a local AAR.

---

## 4. Integrate the Mesh SDK (AAR)

### 4.1 Add the AAR

```
app/
└── libs/
    └── feasyhome-mesh-release.aar     ← provided by Feasycom
```

AAR SHA-256 (verify it to detect tampering):

```
9823A548EBCE6022AAC9AC3EDE67F0FE1D50A5CA8496D3F2DA7D29052D6C029B
```

### 4.2 Gradle dependencies

A local AAR does **not** bring its own transitive dependencies, so declare them explicitly in
`app/build.gradle`:

```groovy
dependencies {
    implementation project(':utilcode')

    // Mesh SDK (local AAR)
    implementation files('libs/feasyhome-mesh-release.aar')

    // —— transitive dependencies of mesh, declared explicitly ——
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

> If the AAR is later published to a private Maven repository as
> `no.nordicsemi.android.mesh:mesh:<version>`, these transitive declarations can be removed.

### 4.3 ProGuard / R8

The AAR carries its own `consumerProguardFiles` (`mesh-proguard-rules.pro`), so integrators do
**not** need extra keep rules for mesh. When enabling `minifyEnabled true`, keep your Room entities
and Gson models reachable (the AAR rules already cover its own).

---

## 5. Initialization

The SDK entry point is the singleton `no.nordicsemi.android.mesh.fsdk.FscMeshSdk.getApi2()`.

```java
public class HomeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Must be called before any other SDK API — put it in Application
        FscMeshSdk.getApi2().Init(this);
        // SDK version, e.g. "2.0.0.5"
        String sdkVer = FscMeshSdk.getApi2().getSdkVer();
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<application android:name=".HomeApp" ... />
```

---

## 6. Mesh network management

A `MeshNetwork` is the container of nodes. An app may manage several networks but only one is active.

### 6.1 Create / query / switch

```java
Api2 api = FscMeshSdk.getApi2();

// create
api.CreateMeshNetwork("MyHome", new FscMeshNetworkCallback() {
    @Override public void onSuccess(MeshNetwork network) { /* created */ }
    @Override public void onFail(String msg) { /* failed */ }
});

// query one network by uuid / query all
api.QueryMeshNetwork(meshUuid, fscMeshNetworkCallback);
api.QueryMeshNetworks(new FscMeshNetworksCallback() {
    @Override public void onSuccess(List<MeshNetwork> networks) { }
    @Override public void onFail(String msg) { }
});

// switch active network
api.SwitchMeshNetwork(meshNetwork);
MeshNetwork current = api.getMeshNetwork();

// delete
api.deleteMeshNetworkFromDb(meshNetwork);
```

### 6.2 Export / import (backup and multi-device sync)

```java
String json = api.exportMeshNetwork();           // export current network
String json2 = api.exportMeshNetwork(network);   // export a specific network
api.importMeshNetworkJson(json);                 // import
api.loadMeshNetwork();                           // reload
```

> The exported JSON contains network keys. **Do not store it in a public location.**

---

## 7. Scanning and provisioning

### 7.1 Scan for unprovisioned devices

```java
api.StartScan(
    20000,              // stopTime: scan duration in ms
    filterUuid,         // filterUuid: Mesh service UUID filter
    (byte) 0x01,        // mdevPid: device PID filter (0 = no filter)
    (byte) 0x00,        // mdevSecondType: device sub-type filter (0 = no filter)
    new FscScancallback() {
        @Override public void onScanResult(ExtendedBluetoothDevice device) {
            // device: MAC / RSSI / advertisement data
        }
        @Override public void onStop() { }
    });

api.StopScan();          // stop early

// make an unprovisioned device blink (by MAC) so the user can identify it
api.OpenSearchAdv("AA:BB:CC:DD:EE:FF");
// broadcast on/off of the unprovisioned indicator
api.OpenIndicatorByAdv(true);
api.CloseAdv();
```

### 7.2 Provision

```java
api.StartProvisionedNode(extendedBluetoothDevice, new ProvisioningCallback() {
    @Override public void onProvisioningStart() { }
    @Override public void onProvisioningProgress(int step, int total) { }
    @Override public void onProvisioningSuccess(ProvisionedMeshNode node) {
        // node.getUnicastAddress() is the assigned unicast address
    }
    @Override public void onProvisioningFailed(String reason) { }
});
```

> Provisioning requires a GATT proxy connection; the SDK connects GATT internally before running
> the provisioning state machine. If the device already belongs to a network, reset it first with
> `ResetNode(unicastAddress)` or `ResetAllNode()`.

---

## 8. Device control

All Mesh control is sent to **group addresses**; a node can subscribe to multiple groups.

### 8.1 Subscribe a node to a group

```java
api.AddNodeToGroup(
    meshNode,                 // ProvisionedMeshNode
    groupAddressBytes,        // group address, 2 bytes e.g. {0xC0, 0x01}
    (byte) 1,                 // isGroupleader: 1 = group leader (BLT_A), 0 = normal node
    new SubscribeGroupCallback() {
        @Override public void onSuccess() { }
        @Override public void onFail(String msg) { }
    });
```

### 8.2 Lights

```java
api.ControlLightALL(groupAddr, lightness, ct);   // lightness + color temperature together
api.ControlLightness(groupAddr, lightness);      // lightness only (0–100)
api.ControlLightct(groupAddr, ct);               // color temperature only
api.ControlDevOnOff(groupAddr, (byte) 1);        // on = 1 / off = 0
```

### 8.3 Curtains

```java
api.ControlCurtainOpenPercent(groupAddr, openPercent);  // open percentage
api.ControlCurtainDirection(groupAddr, direction);      // open / close / stop
api.ControlCurtainCleanMil(groupAddr);                  // clear/calibrate travel
```

### 8.4 Panels (buttons)

```java
api.SetPanelBtnLight(unicastAddress, lightness);                     // backlight
api.AT_GroupBindPanelBtn(unicastAddress, groupAddr, btnId, doType);  // bind button to a group
api.AT_GroupBindColorBtn(unicastAddress, btnId, doType);             // bind button to color control
api.AT_SceneBindPanelBen(unicastAddress, sceneId, btnId, doType);    // bind button to a scene
```

### 8.5 Scenes

```java
api.SceneBindLightGroup(sceneId, groupAddr, lightness, ct);
api.SceneBindCurtainGroup(sceneId, groupAddr, openPercent);
api.SceneUnbindGroup(sceneId, groupAddr);
api.ControlSceneOnOff(sceneId, (byte) 1);
api.DelScene(sceneId);
```

### 8.6 Voice boxes / rooms

```java
api.SyncVoice(unicastAddress, data);
api.SetAssignRoom(unicastAddress, roomId);
api.SynconeB(unicastAddress, roomCode, iv);
```

---

## 9. Node query and reset

```java
ProvisionedMeshNode node = api.getNode(unicastAddress);
api.getNodes(meshUuid, new QuerNodesCallback() {
    @Override public void onSuccess(List<ProvisionedMeshNode> nodes) { }
    @Override public void onFail(String msg) { }
});
api.deleteNode(node);              // remove from DB only (does not reset the device)
api.ResetNode(unicastAddress);     // reset one node (back to unprovisioned)
api.ResetNodeAT(unicastAddress);   // reset (AT channel variant)
api.ResetAllNode();
api.ResetAllNode2();
```

---

## 10. Connection and state

```java
api.ConnectNetWork(networkId, new FscConnectCallback() {
    @Override public void onConnected() { }
    @Override public void onDisconnected() { }
    @Override public void onConnectFailed(String msg) { }
});

api.isConnect();          // proxy connected?
api.getConnectedAdr();    // address of the connected node
api.Disconnect();
api.setClearCacheRequired();  // clear GATT cache before next connection
```

Low-level BLE manager (`BleMeshManager`, rarely used directly):

| Method | Description |
|---|---|
| `sendPdu(byte[] pdu)` | send a PDU (used internally) |
| `getMaximumPacketSize()` | max payload bytes for the current MTU |
| `isProvisioningComplete()` | provisioning finished? |
| `isDeviceReady()` | device ready? |

---

## 11. Permissions

Declared in `app/src/main/AndroidManifest.xml`:

| Permission | Purpose |
|---|---|
| `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `BLUETOOTH_ADVERTISE` | BLE scan/connect/advertise on Android 12+ |
| `BLUETOOTH` / `BLUETOOTH_ADMIN` | BLE on Android 11 and below |
| `ACCESS_FINE_LOCATION` | BLE scanning (required up to Android 11) |
| `INTERNET` / `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE` | account, family, statistics |
| `FOREGROUND_SERVICE` | keep the Mesh connection alive |
| `CAMERA` | QR code scanning to join a family |
| `READ/WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE` | network export, logs |

BLE and location permissions must be requested at runtime (the project uses **XXPermissions**).

---

## 12. Build and signing

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

**Signing:** `signingConfigs` has been removed from `app/build.gradle` (the keystore and passwords
are internal resources). To produce a signed APK, add your own configuration:

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

⚠️ Never commit a keystore or its passwords (`*.jks` / `*.keystore` are ignored by `.gitignore`).

---

## 13. Troubleshooting

| Symptom | Check |
|---|---|
| `SDK location not found` | configure `sdk.dir` in `local.properties` or `ANDROID_HOME` |
| `no.nordicsemi.android.mesh.*` classes not found | AAR present in `app/libs/` and `implementation files(...)` configured |
| Room/Gson/Nordic BLE classes missing | the transitive dependencies from 4.2 are incomplete |
| No devices found while scanning | Bluetooth on, location on, BLE permissions granted, device not connected elsewhere |
| Provisioning always fails | device may already be provisioned → `ResetNode` and retry; move closer; check PID filter |
| Control commands do nothing | check `isConnect()`, group address, and that the node subscribed to that group |
| Multiple phones out of sync | use export/import (SDK 2.0.0.4+) and `SwitchMeshNetwork` to share the same network |

---

## 14. SDK changelog

| Version | Changes |
|---|---|
| 2.0.0.5 | added statistics interfaces |
| 2.0.0.4 | fixed multi-phone control issues and known bugs |
| 2.0.0.3 | bug fixes |
| 2.0.0.2 | provisioning for panels and voice boxes |
| 2.0.0.1 | provisioning for lights and curtains |
| 2.0.0.0 | first release |

---

## 15. Key classes

| Class | Package | Purpose |
|---|---|---|
| `FscMeshSdk` | `no.nordicsemi.android.mesh.fsdk` | SDK entry point, `getApi2()` |
| `Api2` | `no.nordicsemi.android.mesh.fsdk` | High-level API used in sections 6–10 |
| `MeshManagerApi` | `no.nordicsemi.android.mesh` | Low-level mesh stack (provisioning, PDUs, export) |
| `MeshMngrApi` | `no.nordicsemi.android.mesh` | Interface of `MeshManagerApi` |
| `BleMeshManager` | `no.nordicsemi.android.mesh.fsdk` | GATT connection and PDU transport |
| `ProvisionedMeshNode` | `no.nordicsemi.android.mesh.transport` | Provisioned node model |
| `UnprovisionedMeshNode` | `no.nordicsemi.android.mesh.provisionerstates` | Unprovisioned node model |
| `ExtendedBluetoothDevice` | `no.nordicsemi.android.mesh.fsdk` | Scan result (MAC / RSSI) |
