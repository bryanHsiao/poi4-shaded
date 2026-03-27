# 共存測試說明

驗證 Shaded POI 4（`poi4shaded.*`）能與全域 POI 3（`org.apache.poi`）同時載入而不衝突。

## 前置條件

- Java JDK 已安裝且 `javac`、`java` 可在命令列使用
- POI 3 的 JAR 存在於 `C:\IBM\NotesR9\jvm\lib\ext\`：
  - `poi-3.16.jar`
  - `poi-ooxml-3.16.jar`
  - `poi-ooxml-schemas-3.16.jar`
- 已執行過 `mvn clean package`，`target\poi4-shaded-4.1.1.jar` 存在

## 執行方式

到 `test` 資料夾雙擊 `run-test.bat`，或在命令列執行：

```bat
cd test
run-test.bat
```

## 預期結果

```
=== POI 3 + Shaded POI 4 共存測試 ===

[POI 3] 測試開始...
[POI 3] 版本: 3.16
[POI 3] 成功產出 poi3_test.xls

[Shaded POI 4] 測試開始...
[Shaded POI 4] 版本: 4.1.1
[Shaded POI 4] 成功產出 poi4_shaded_test.xlsx

=== 測試結果 ===
POI 3 (org.apache.poi)          : PASS
Shaded POI 4 (poi4shaded.*)     : PASS
共存測試                         : PASS
```

三項皆 PASS 代表兩個版本可正常共存。測試完成後會在 `test` 資料夾產出 `poi3_test.xls` 和 `poi4_shaded_test.xlsx`，可開啟確認內容。

## 測試原理

| 項目 | Package 來源 | 動作 |
|---|---|---|
| POI 3 | `org.apache.poi`（來自 `poi-3.16.jar`） | 建立 `.xls` 檔案 |
| Shaded POI 4 | `poi4shaded.org.apache.poi`（來自 `poi4-shaded-4.1.1.jar`） | 建立 `.xlsx` 檔案 |

因為 shaded JAR 的 package name 已改為 `poi4shaded.*`，與 POI 3 的 `org.apache.poi.*` 不同，JVM 會視為完全不同的類別，因此不會衝突。

---

## Domino Java Agent 範例

`DominoAgentExample.java` 示範如何在 HCL Domino 環境中使用 shaded POI 4 讀取文件附件。

### 功能

1. 透過 UNID 取得指定文件
2. 從 Rich Text 欄位取出附件
3. 使用 `FileMagic` 自動偵測檔案格式（OLE2 / OOXML）
4. 根據格式與副檔名分別處理：

| 格式 | 副檔名 | 使用的 Class |
|---|---|---|
| OLE2 | `.xls` | `poi4shaded.org.apache.poi.hssf.usermodel.HSSFWorkbook` |
| OLE2 | `.doc` | `poi4shaded.org.apache.poi.hwpf.HWPFDocument` |
| OOXML | `.xlsx` | `poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook` |
| OOXML | `.docx` | `poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument` |

### 使用方式

1. 將 `poi4-shaded-4.1.1.jar` 放入 Domino 的 `jvm\lib\ext`（12.0.x 及更早）或 `ndext`（14.0+）
2. 在 Domino Designer 建立 Java Agent，貼入 `DominoAgentExample.java` 的程式碼
3. 修改 `UNID` 為目標文件的 UNID
4. 修改 `FIELD_NAME` 為存放附件的 Rich Text 欄位名稱
5. 執行 Agent

### import 規則

所有原本 `org.apache.poi.*` 的 import，前面加上 `poi4shaded.` 即可：

```java
// 原本 POI 4
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Shaded POI 4
import poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook;
```
