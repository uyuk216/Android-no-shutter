# NoShutter

Android のカメラ「シャッター音」を無効化する設定変更ツール(Kotlin / Compose, minSdk 33)。

## 仕組み
メーカー独自の「シャッター音を強制する」設定値を `WRITE_SECURE_SETTINGS` で書き換えます。
機種ごとの設定キーは [`app/src/main/assets/profiles.json`](app/src/main/assets/profiles.json) に定義します。

## セットアップ(一度だけ)
次のどちらかで権限を付与します。
- PC: `adb shell pm grant dev.noshutter android.permission.WRITE_SECURE_SETTINGS`
- 端末のみ: [Shizuku](https://shizuku.rikka.app/) を起動し、アプリの「Shizukuで付与」を押す

## 対応状況
| メーカー | 状態 |
|---|---|
| Samsung Galaxy | 未検証(キー: `csc_pref_camera_forced_shuttersound_key`) |
| Pixel / OPPO / Xiaomi / Motorola / FCNT ほか | プロファイル募集中 |

地域・機種によっては設定値では消せない場合があります(その場合は無効化に失敗と表示されます)。

## 新しい機種を追加するには
1. アプリの「探索」(Shizuku必須)でカメラ音関連のキーを一覧する
2. 値を切り替えて実機で音が消えることを確認
3. `profiles.json` にエントリを追加して PR(`verified: true` は実機確認後のみ)

## 注意
無断撮影・盗撮防止の観点で、シャッター音の無効化が法令や規約に抵触する地域があります。自己責任で利用してください。
