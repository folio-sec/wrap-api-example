# サンプルラップサービス

## 開発

- Java 17+

**macOS / Linux:**
```shell
# 準備
git init
git add .
git commit -m init

# セットアップ
./mvnw test-compile

# テスト実行
./mvnw test

# 事前コマンド確認
command -v curl
command -v patch
```

**Windows (PowerShell):**
```powershell
# 準備
git init
git add .
git commit -m init

# セットアップ
.\mvnw.cmd test-compile

# テスト実行
.\mvnw.cmd test

# 事前コマンド確認
Get-Command curl.exe -ErrorAction Stop
Get-Command git -ErrorAction Stop
```
