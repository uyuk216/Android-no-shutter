# NoShutter

日本などで強制されるカメラのシャッター音を、root なしで無効化するツール(Kotlin, minSdk 33)。

## 方式
shell 権限(`MODIFY_AUDIO_ROUTING` を持つ)で `AudioSystem.setForceUse(FOR_SYSTEM, FORCE_NONE)` を呼び、
音声ポリシーの「シャッター音強制」を解除します。メーカー固有の設定キーは使いません。
変更は audioserver に残り、再起動で元に戻ります。アンインストールでは戻らないので、先に「元に戻す」を実行してください。

## ロードマップ
1. **(今ここ)** PC の adb から `app_process` で実行して、実機で音が消えることを確認
2. アプリ内蔵の ADB クライアント(ワイヤレスデバッグ)で、PC・Shizuku なしで実行
3. 起動時の自動再適用、機種別の動作報告表

## インストール
[Releases](https://github.com/uyuk216/Android-no-shutter/releases) から最新の `NoShutter-vX.Y.Z.apk` をダウンロードしてインストールします。
アプリでワイヤレスデバッグのペア設定(初回のみ)をしたあと、「無効化」を押します。

## リリース手順(開発者向け)
`v0.2.0` のようなタグを push すると、GitHub Actions が APK をビルドして Release に添付します。
署名鍵は Secrets(`SIGNING_KEYSTORE_BASE64` / `SIGNING_STORE_PASSWORD` / `SIGNING_KEY_ALIAS` / `SIGNING_KEY_PASSWORD`)から読みます。
未設定だとビルドごとに違うデバッグ鍵で署名され、上書き更新できないので、配布前に設定してください。

## PC の adb から試す
```sh
adb install app-debug.apk
adb shell 'CLASSPATH=$(pm path dev.noshutter | cut -d: -f2) app_process /system/bin dev.noshutter.ShellMain mute'
adb shell 'CLASSPATH=$(pm path dev.noshutter | cut -d: -f2) app_process /system/bin dev.noshutter.ShellMain restore'
```
root 端末では、端末上のターミナルで同じコマンドを `su -c '...'` で実行しても動きます。
`restore` は端末本来の状態(強制する端末なら `11`、しない端末なら `0`)に戻します。

`FOR_SYSTEM=0` なら強制解除、`11` なら強制中です。ColorOS(OPPO/OnePlus/realme)は、開発者オプションの「権限監視を無効にする」が必要な場合があります。

## ローカル開発
```sh
git clone https://github.com/uyuk216/Android-no-shutter
cd Android-no-shutter
git checkout claude/prototype-audio-forceuse
```
1. Android Studio(JDK 17 同梱)でフォルダを開く。SDK は Platform 35 を入れる。
2. Gradle Wrapper が未生成なので、一度だけ `gradle wrapper --gradle-version 8.10.2` を実行する(Android Studio の同期でも可)。
3. 実機を USB デバッグで接続し、`./gradlew installDebug` で入れる。
4. 上の「マイルストーン1の試し方」のコマンドで動作を確認する。

## 謝辞
同じ方式を実装している [soralis0912/ShutterSoundToggle](https://github.com/soralis0912/ShutterSoundToggle)(NYSL)を参考にしています。

## 注意
無断撮影・盗撮防止の観点で、法令や規約に抵触する地域があります。自己責任で利用してください。
