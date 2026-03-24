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
