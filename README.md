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

## マイルストーン1の試し方
```sh
adb install app-debug.apk
adb shell 'CLASSPATH=$(pm path dev.noshutter | cut -d: -f2) app_process /system/bin dev.noshutter.ShellMain mute'
adb shell 'CLASSPATH=$(pm path dev.noshutter | cut -d: -f2) app_process /system/bin dev.noshutter.ShellMain restore'
```
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
