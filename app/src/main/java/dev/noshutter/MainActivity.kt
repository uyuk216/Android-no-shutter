package dev.noshutter

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val controller = ShutterController(this)
        val profiles = Profile.loadAll(this)
        setContent { MaterialTheme { Screen(controller, profiles) } }
    }

    @Composable
    private fun Screen(c: ShutterController, profiles: List<Profile>) {
        val profile = remember { profiles.firstOrNull { it.matchesThisDevice() } }
        var hasPerm by remember { mutableStateOf(c.hasSecureSettingsPermission()) }
        var muted by remember { mutableStateOf(profile?.let { c.isMuted(it) } ?: false) }
        var log by remember { mutableStateOf("") }
        val adb = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"

        Column(
            Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text("NoShutter", style = MaterialTheme.typography.headlineMedium)
            Text("${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE}")
            Text(
                when {
                    profile == null -> "この機種向けプロファイルがありません。下の「探索」で候補キーを調べてください。"
                    profile.verified -> "プロファイル: ${profile.name}(動作確認済み)"
                    else -> "プロファイル: ${profile.name}(未検証・実機での報告歓迎)"
                }
            )

            if (!hasPerm) {
                Text("必要な権限 WRITE_SECURE_SETTINGS が未付与です。次のどちらかで一度だけ付与してください。")
                Text("A) PCから: $adb")
                Button(onClick = {
                    if (!ShizukuHelper.isRunning()) log = "Shizukuが起動していません"
                    else if (!ShizukuHelper.hasPermission()) {
                        ShizukuHelper.requestPermission(); log = "Shizukuの許可後にもう一度押してください"
                    } else {
                        Thread {
                            runCatching { ShizukuHelper.grantSecureSettings(packageName) }
                                .onFailure { log = "付与失敗: ${it.message}" }
                            hasPerm = c.hasSecureSettingsPermission()
                        }.start()
                    }
                }) { Text("B) Shizukuで付与") }
            }

            if (profile != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = hasPerm, onClick = {
                        val ok = c.apply(profile, true); muted = c.isMuted(profile)
                        log = if (ok) "シャッター音を無効化しました" else "失敗(この機種では設定が効かない可能性)"
                    }) { Text("無効化") }
                    OutlinedButton(enabled = hasPerm, onClick = {
                        val ok = c.apply(profile, false); muted = c.isMuted(profile)
                        log = if (ok) "元に戻しました" else "失敗"
                    }) { Text("元に戻す") }
                }
                Text("現在: " + if (muted) "無効(設定値が無効化状態)" else "有効または未確認")
            }

            OutlinedButton(onClick = {
                Thread {
                    log = runCatching { ShizukuHelper.probe().joinToString("\n").ifEmpty { "該当キーなし" } }
                        .getOrElse { "探索にはShizukuが必要: ${it.message}" }
                }.start()
            }) { Text("探索(カメラ音関連の設定キーを一覧)") }

            if (log.isNotEmpty()) Text(log)
            Text(
                "注意: 多くの国で盗撮防止のため、シャッター音の無効化は法令・規約に反する場合があります。" +
                    "自己責任で、撮影対象の同意を得て利用してください。",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
