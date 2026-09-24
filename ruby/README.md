# サンプルラップサービス

## 開発

- Ruby 3.2+ (or Ruby 4.0+)

```shell
# 準備
git init
git add .
git commit -m init

# setup
bundle config set --local path vendor/bundle
bundle install

# test
bundle exec rspec

# 事前コマンド確認
command -v curl
command -v patch
```
