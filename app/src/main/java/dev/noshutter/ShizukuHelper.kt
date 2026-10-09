package dev.noshutter

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/** Runs shell commands through Shizuku so the user does not need a PC for the one-time grant. */
object ShizukuHelper {
    const val REQUEST_CODE = 1001

    fun isRunning() = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun hasPermission() = isRunning() &&
        runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)

    fun requestPermission() = Shizuku.requestPermission(REQUEST_CODE)

    // Shizuku.newProcess is package-private in the API; reflection is the commonly used workaround.
    fun exec(vararg cmd: String): String {
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java,
        ).apply { isAccessible = true }
        val p = m.invoke(null, cmd, null, null) as Process
        val out = p.inputStream.bufferedReader().readText() + p.errorStream.bufferedReader().readText()
        p.waitFor()
        return out
    }

    fun grantSecureSettings(pkg: String) = exec("pm", "grant", pkg, "android.permission.WRITE_SECURE_SETTINGS")

    /** Lists settings whose key looks camera/shutter related, to help add profiles for new devices. */
    fun probe(): List<String> = listOf("system", "secure", "global").flatMap { table ->
        exec("settings", "list", table).lineSequence()
            .filter { Regex("shutter|camera.*sound|sound.*camera|shuttersound", RegexOption.IGNORE_CASE).containsMatchIn(it) }
            .map { "$table: $it" }.toList()
    }
}
