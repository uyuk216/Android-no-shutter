package dev.noshutter

import org.lsposed.hiddenapibypass.HiddenApiBypass
import kotlin.system.exitProcess

/**
 * Entry point run as the shell user (uid 2000), which holds MODIFY_AUDIO_ROUTING:
 *
 *   CLASSPATH=$(pm path dev.noshutter | cut -d: -f2) app_process /system/bin dev.noshutter.ShellMain <get|mute|restore|default>
 *
 * Can be launched from a PC's adb, Shizuku, or an on-device ADB connection.
 * The change lives in audioserver, so this process exits right after the call.
 */
object ShellMain {
    @JvmStatic
    fun main(args: Array<String>) {
        HiddenApiBypass.addHiddenApiExemptions("Landroid/media/AudioSystem;")
        val audioSystem = Class.forName("android.media.AudioSystem")
        val get = audioSystem.getMethod("getForceUse", Int::class.java)
        val set = audioSystem.getMethod("setForceUse", Int::class.java, Int::class.java)

        fun apply(config: Int) {
            val status = set.invoke(null, ForceUse.FOR_SYSTEM, config) as Int
            if (status != 0) { println("ERROR setForceUse=$status"); exitProcess(1) }
        }

        when (args.firstOrNull()) {
            "mute" -> apply(ForceUse.FORCE_NONE)
            // Restore to what this device would choose itself, not a hard-coded "enforced".
            "restore", "default" -> apply(ForceUse.deviceDefault())
            "get" -> {}
            else -> { println("usage: get|mute|restore|default"); exitProcess(2) }
        }
        println("FOR_SYSTEM=${get.invoke(null, ForceUse.FOR_SYSTEM)}")
        exitProcess(0)
    }
}
