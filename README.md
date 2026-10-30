# Quick Launcher

Android向けのシンプルなアプリ起動ツールです。ホームアプリ（標準ランチャー）を置き換えません。

## 機能
- 起動可能なインストール済みアプリを自動取得
- アプリアイコンと名前を一覧表示
- 入力に応じたアプリ名検索
- タップでアプリを起動

## GitHub Actionsでビルド
`.github/workflows/android.yml` が push / pull request / 手動実行で Android Debug APK をビルドします。

1. GitHub リポジトリの **Actions** タブを開く
2. **Android APK** ワークフローを選ぶ
3. 実行が完了したら、実行結果ページ下部の **Artifacts** から `QuickLauncher-debug-apk` をダウンロード
4. ZIPを展開すると `app-debug.apk` が入っています

## ローカルビルド
Android Studioでリポジトリを開き、Gradle Sync後に `assembleDebug` を実行してください。

## 技術情報
- Kotlin
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.9
- compileSdk 35 / minSdk 23 / targetSdk 35
- JDK 17

Android 11以降のパッケージ可視性に対応するため、`<queries>` で `MAIN` / `LAUNCHER` の起動可能アクティビティを照会します。`QUERY_ALL_PACKAGES` は要求しません。
