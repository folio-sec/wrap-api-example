# サンプルラップサービス

## 開発

- Python 3.9+

**macOS / Linux:**
```shell
# 準備
git init
git add .
git commit -m init

# setup
python -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"

# test
pytest -v

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
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -e ".[dev]"

# test
pytest -v

# 事前コマンド確認
Get-Command curl.exe -ErrorAction Stop
Get-Command git -ErrorAction Stop
```
