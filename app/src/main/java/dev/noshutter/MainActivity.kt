package dev.noshutter

import android.content.ClipData
import android.content.ClipboardManager
import android.media.MediaActionSound
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen() } }
    }

    private fun cmd(action: String) =
        "CLASSPATH=\$(pm path $packageName | cut -d: -f2) app_process /system/bin dev.noshutter.ShellMain $action"

    @Composable
    private fun Screen() {
        val enforced = remember { MediaActionSound.mustPlayShutterSound() }
        Column(
            Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text("NoShutter (prototype)", style = MaterialTheme.typography.headlineMedium)
            Text("${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE}")
            Text("この端末はシャッター音を強制している: $enforced")
            Text("現在はマイルストーン1です。PCのadbから次のコマンドを実行して、実機で音が消えるか確認してください。")
            for ((label, action) in listOf("無効化" to "mute", "元に戻す" to "restore", "状態確認" to "get")) {
                OutlinedButton(onClick = {
                    val cm = getSystemService(ClipboardManager::class.java)
                    cm.setPrimaryClip(ClipData.newPlainText("cmd", "adb shell '${cmd(action)}'"))
                }) { Text("$label のコマンドをコピー") }
            }
            Text(
                "再起動すると元に戻ります。無断撮影・盗撮防止の観点で、法令や規約に反する場合があります。自己責任で利用してください。",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
