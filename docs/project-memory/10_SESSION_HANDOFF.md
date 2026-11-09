# Session handoff — testOS

**Последнее обновление:** 2026-11-09 09:25 +05:00  
**Проект:** `gonscharovd/tvOS`  
**Кодовая точка:** PR #14, `refactor/testos-core-cleanup-2026-11`, commit `fa91b18305e57bae536d6664af88cb6230a4ec7e`  
**Документационная точка:** PR #15, `docs/project-memory-2026-11` (проверять актуальный branch HEAD в GitHub; этот handoff входит в тот же PR)

## Контекст

Выполнена TOS-002: единый источник version metadata для Bash, PowerShell и GitHub Actions. Оба build scripts собрали APK на соответствующих CI runners. PR #14 остаётся Draft, merge/OTA/TV install не выполнялись.

## Что проверено

- Прочитаны project-memory инструкции, актуальное состояние, список задач и предыдущий handoff.
- Проверены PR #14/#15, ветки, main и актуальные GitHub Actions.
- Изучены `build.sh`, `build.ps1`, `build-apk.yml`, `release.yml`, `tests/test_build_contract.py` и соответствующие docs.
- Проверен текущий version contract: development defaults 2.1.74 / versionCode 89; release base 15, prefix 2.1.; v2.1.73 → 88.
- PowerShell contract test прошёл на Windows runner и Linux PowerShell Core.
- Python build-contract regression прошёл: Bash defaults/overrides, workflow wiring, release formula, artifact/signing/output contracts.
- CI run #313 прошёл целиком: Ubuntu/Linux и Windows jobs; обе сборки APK, подписи и package/version metadata проверены; два debug artifacts загружены.
- Полная Windows сборка подтвердила работоспособность `build.ps1` на Windows Server 2025 runner.
- Не выполнялись локальная сборка на ПК пользователя и runtime test на физическом ТВ.

## Что изменено в PR #14

- Добавлен `version.properties`:
  - `DEFAULT_VERSION_CODE=89`
  - `DEFAULT_VERSION_NAME=2.1.74`
  - `RELEASE_VERSION_CODE_BASE=15`
  - `RELEASE_VERSION_PREFIX=2.1.`
- `build.sh` читает этот файл и сохраняет приоритет environment overrides.
- `build.ps1` использует общий PowerShell helper `tools/version.ps1`; environment overrides сохраняются.
- Добавлен `tests/test_build_contract.ps1` для defaults и overrides.
- Расширен `tests/test_build_contract.py`: проверяет Bash defaults/overrides, PowerShell wiring, CI/release workflow contract, формулу 15 + PATCH, output/assets/signing controls.
- `build-apk.yml` теперь имеет Linux и Windows jobs; проверяет package/version/signature и загружает отдельные debug artifacts.
- `release.yml` получает release base/prefix из version.properties; формула 15 + PATCH сохранена.
- PR #14 description обновлён с результатами run #313 и оставшимися ограничениями.

## Подтверждённое состояние GitHub

- `main`: `a765318b577e66fae66a8ffe28439dcd6233f38f` — не изменён.
- PR #14: Open / Draft, base main, head `fa91b18305e57bae536d6664af88cb6230a4ec7e`.
- Последний CI: [run #313](https://github.com/gonscharovd/tvOS/actions/runs/42561116519), success, commit `fa91b18...`.
- Jobs run #313: Ubuntu/Linux success; Windows success.
- Artifacts: `testOS-ci-debug` ID `13648674193`; `testOS-ci-debug-windows` ID `13649636643`. Оба подписаны runner-generated debug keys, не являются OTA-compatible гарантированно.
- PR #15: Open / Draft, base — ветка PR #14; актуальный docs branch HEAD проверяется непосредственно в GitHub.
- Stable release: v2.1.73 / versionCode 88.
- Ни один PR не влит; OTA не выпущена; APK на ТВ не устанавливался.

## Осталось

- PR #15 документационные изменения ожидают review.
- TOS-004: удалить hard-coded JDK fallback path из build.ps1; использовать JAVA_HOME/PATH и сохранить текущую Windows build validation.
- TOS-008: подключить `SingleUserMigrationTest.java` к CI.
- Runtime regression PR #14 на Sber SDX-50U4123B не выполнена.
- Сертификат установленного на ТВ APK не сверялся с release certificate.
- HOME component подтверждён по compiled APK manifest, но PackageManager command на физическом ТВ не тестировалась.
- CI debug artifacts не устанавливать поверх release APK без сравнения сертификатов.

## NEXT ACTION

**Выполнить TOS-004: удалить machine-specific JDK fallback из build.ps1, оставить приоритет JAVA_HOME и поиск javac в PATH, затем проверить Windows CI build и обновить project memory.**
