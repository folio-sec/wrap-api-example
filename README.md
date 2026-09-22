# Wrap API Example

## 面接課題の雛形と patch

各言語ディレクトリ(`golang/`, `java17/`, ...)は面接前に候補者へ配布できる **雛形** で、ビルド・テスト手順と PortfolioScenario までを含みます。
面接課題そのもの(サービス概要・課題文・リバランス実装・OrderScenario)は言語別の patch として管理し、面接開始時に雛形へ適用します。

| パス | 役割 |
| --- | --- |
| `<language>/` | 雛形。CI ではこの状態と patch 適用後の両方を検証する |
| `patches/<language>.patch` | 雛形に適用すると従来の面接課題が完全に復元される patch |
| `interview/manifests/<language>.txt` | patch が変更してよいファイルの一覧。範囲外に触れる patch は CI が拒否する |
| `interview/shared/README.challenge.md` | 全言語共通の課題文。patch 適用後の README 末尾と一致していなければ CI が失敗する |

| スクリプト | 用途 |
| --- | --- |
| `scripts/verify-interview-template.sh <language> [dir]` | 雛形に課題の内容が混入していないことを確認する |
| `scripts/verify-interview-patch.sh <language> [dir]` | patch が manifest の範囲内で、適用すると課題が復元されることを確認する |
| `scripts/apply-interview-patch.sh <language> [dir]` | 雛形に patch を適用する(CI と Release で使用) |
| `scripts/generate-interview-patch.sh <language> [assembled-dir] [output]` | patch を生成する。`assembled-dir` を省略すると現在の patch と `README.challenge.md` から再生成する |
| `scripts/build-release-assets.sh <output-dir>` | Release 用の zip・patch・checksum を生成する |

### patch の更新手順

課題文(`interview/shared/README.challenge.md`)や雛形側のファイルを編集したときは、コンテキスト行が変わるため patch を再生成してコミットします。

```sh
for language in golang java17 java8 php python ruby scala typescript; do
  ./scripts/generate-interview-patch.sh "$language"
done
```

課題側のコード(リバランス実装・OrderScenario など)を変更するときは、雛形をコピーして patch を適用した作業ディレクトリを編集し、そこから patch を生成します。
新しいファイルを追加した場合は `interview/manifests/<language>.txt` にも追記してください。

```sh
work="$(mktemp -d)/golang"
cp -R golang "$work"
./scripts/apply-interview-patch.sh golang "$work"
# "$work" を編集する
./scripts/generate-interview-patch.sh golang "$work"
```

### 候補者側での patch の適用

Release の `<language>.patch` は、展開した言語ディレクトリ(README.md があるディレクトリ)で `patch -p1` を使って適用します。

```sh
curl -fsSL "https://github.com/folio-sec/wrap-api-example/releases/download/<tag>/<language>.patch" | patch -p1
```

`git apply` でも適用できますが、言語ディレクトリより上の階層で `git init` している場合、
`git apply` はパスをリポジトリルート基準で解釈し、カレントディレクトリ外のファイルを exit 0 のまま黙ってスキップします。
`patch -p1` は常にカレントディレクトリ基準で動作するため、こちらを案内しています。

## Release

Kick the [release action](https://github.com/folio-sec/wrap-api-example/actions/workflows/release.yml) manually, then release.

The workflow runs every language workflow first and then publishes, per language, `<language>-template.zip`, `<language>.patch`, and the assembled `<language>.zip`, together with `SHA256SUMS`.

### DCO Sign-Off Methods

The sign-off is a simple line at the end of the explanation for the patch, which certifies that you wrote it or otherwise have the right to pass it on as an open-source patch.

The DCO requires a sign-off message in the following format appear on each commit in the pull request:

```txt
Signed-off-by: Sample Developer sample@example.com
```

The text can either be manually added to your commit body, or you can add either `-s` or `--signoff` to your usual `git` commit commands.

#### Auto sign-off

The following method is examples only and are not mandatory.

```sh
touch .git/hooks/prepare-commit-msg
chmod +x .git/hooks/prepare-commit-msg
```

Edit the `prepare-commit-msg` file like:

```sh
#!/bin/sh

name=$(git config user.name)
email=$(git config user.email)

if [ -z "${name}" ]; then
  echo "empty git config user.name"
  exit 1
fi

if [ -z "${email}" ]; then
  echo "empty git config user.email"
  exit 1
fi

git interpret-trailers --if-exists doNothing --trailer \
    "Signed-off-by: ${name} <${email}>" \
    --in-place "$1"
```
