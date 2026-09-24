# サンプルラップサービス

## 開発

- PHP 8.1+
- ext-bcmath

**macOS / Linux:**
```shell
# 準備
git init
git add .
git commit -m init

# setup
composer install

# test
vendor/bin/phpunit

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

# setup
composer install

# test
vendor\bin\phpunit

# 事前コマンド確認
Get-Command curl.exe -ErrorAction Stop
Get-Command git -ErrorAction Stop
```
