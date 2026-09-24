# Wrap API Example

## files types

| Path | Role |
| --- | --- |
| `<language>/` | template |
| `patches/<language>.patch` | interview patch |
| `interview/manifests/<language>.txt` | patch editable files |
| `interview/shared/README.challenge.md` | shared patch |

## Development

When changing `interview/shared/README.challenge.md` or template files.

```sh
for language in golang java17 java8 php python ruby scala typescript; do
  ./scripts/generate-interview-patch.sh "$language"
done
```

When changing interview implementations, do followings

```sh
work="$(mktemp -d)/golang"
cp -R golang "$work"
./scripts/apply-interview-patch.sh golang "$work"
# edit "$work"
./scripts/generate-interview-patch.sh golang "$work"
# when adding new file, edit interview/manifests/<language>.txt
```

## Release

Kick the [release action](https://github.com/folio-sec/wrap-api-example/actions/workflows/release.yml) manually, then release.

Each release has a sequential tag (`v1`, `v2`, ...). Its assets are:

- `<language>-template-vN.zip` — share this before the interview
- `<language>-vN.patch` — apply this version's patch during the interview
- `<language>-vN.zip` — completed alternative if applying the patch is difficult

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
