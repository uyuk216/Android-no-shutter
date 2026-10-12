package dev.noshutter

import android.content.Intent
import android.media.MediaActionSound
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen() } }
    }

    @Composable
    private fun Screen() {
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var port by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }

        fun work(block: () -> String) {
            busy = true
            status = "実行中…"
            scope.launch {
                status = withContext(Dispatchers.IO) {
                    runCatching(block).getOrElse { "失敗: ${it.message ?: it}" }
                }
                busy = false
            }
        }

        Column(
            Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text("NoShutter", style = MaterialTheme.typography.headlineMedium)
            Text("${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE}")
            Text("端末の既定: シャッター音を強制${if (MediaActionSound.mustPlayShutterSound()) "する" else "しない"}")

            Text("1. ペア設定(初回のみ)", style = MaterialTheme.typography.titleMedium)
            Text("開発者オプション → ワイヤレスデバッグ → 「ペア設定コードによるデバイスのペア設定」を開き、表示されたポート番号とコードを入力します。分割画面にすると入力しやすいです。")
            OutlinedButton(onClick = {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }) { Text("開発者オプションを開く") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(port, { port = it.filter(Char::isDigit) }, Modifier.weight(1f),
                    label = { Text("ポート") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(code, { code = it.filter(Char::isDigit) }, Modifier.weight(1f),
                    label = { Text("コード") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Button(enabled = !busy && port.isNotEmpty() && code.length == 6, onClick = {
                work {
                    if (LocalAdb.get(this@MainActivity).pairWith(port.toInt(), code)) "ペア設定できました" else "ペア設定に失敗しました"
                }
            }) { Text("ペア設定") }

            Text("2. 実行(ワイヤレスデバッグがオンの間)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((label, action) in listOf("無効化" to "mute", "元に戻す" to "restore", "状態確認" to "get")) {
                    Button(enabled = !busy, onClick = {
                        work { LocalAdb.get(this@MainActivity).run(this@MainActivity, action) }
                    }) { Text(label) }
                }
            }
            if (status.isNotEmpty()) Text(status)
            Text("FOR_SYSTEM=0 なら強制解除、11 なら強制中です。再起動すると元に戻ります。")

            Text(
                "無断撮影・盗撮防止の観点で、法令や規約に反する場合があります。自己責任で利用してください。",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
