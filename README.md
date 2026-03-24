# poi4-shaded

將 Apache POI 4.1.1 及其所有傳遞依賴透過 maven-shade-plugin relocate 到 `poi4shaded.*` 命名空間，產出單一 uber JAR。

## 原由

HCL Domino 的 Java 執行環境會優先從 `jvm\lib\ext`（12.0.x 及更早）或 `ndext`（14.0+）載入 JAR。當全域已部署 **POI 3.x** 時，若有新功能需要使用 **POI 4.x**，兩者的 class 會因為 package name 相同而衝突。

本專案將 POI 4.1.1 所有相關 package 重新打包（shade）至 `poi4shaded.*`，使新功能引用 shaded JAR 即可與全域 POI 3.x 共存，無需替換或升級既有環境。

## 類別載入順序（Domino）

1. `jvm\lib\ext` 或 `ndext` 路徑下的 JAR
2. NSF 資料庫中儲存的 JAR
3. `JavaUserClasses` notes.ini 參數指向的 JAR
4. 以上皆找不到 → `NoClassDefFoundError`

## 使用方式

```bash
mvn clean package
```

產出檔案：`target/poi4-shaded-4.1.1.jar`

將此 JAR 放入 Domino 的 `jvm\lib\ext`（或 `ndext`）、NSF 或 `JavaUserClasses` 路徑中即可。

## Relocated Packages

| 原始 Package | Shaded Package |
|---|---|
| `org.apache.poi` | `poi4shaded.org.apache.poi` |
| `org.apache.xmlbeans` | `poi4shaded.org.apache.xmlbeans` |
| `org.openxmlformats.schemas` | `poi4shaded.org.openxmlformats.schemas` |
| `com.microsoft.schemas` | `poi4shaded.com.microsoft.schemas` |
| `org.apache.commons.compress` | `poi4shaded.org.apache.commons.compress` |
| `org.apache.commons.collections4` | `poi4shaded.org.apache.commons.collections4` |
| `org.apache.commons.codec` | `poi4shaded.org.apache.commons.codec` |
| `org.apache.commons.math3` | `poi4shaded.org.apache.commons.math3` |
| 其他（詳見 pom.xml） | `poi4shaded.*` |

## 程式碼引用範例

```java
// 原本 POI 4 的 import
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// 改用 shaded 版本
import poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook;
```
