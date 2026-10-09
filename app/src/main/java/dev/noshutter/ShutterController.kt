package dev.noshutter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/** Reads/writes the vendor settings that force the camera shutter sound. */
class ShutterController(private val context: Context) {

    fun hasSecureSettingsPermission() =
        context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    fun read(e: SettingEntry): String? = when (e.table) {
        "system" -> Settings.System.getString(context.contentResolver, e.key)
        "secure" -> Settings.Secure.getString(context.contentResolver, e.key)
        "global" -> Settings.Global.getString(context.contentResolver, e.key)
        else -> null
    }

    private fun write(e: SettingEntry, value: String): Boolean {
        val cr = context.contentResolver
        return when (e.table) {
            "system" -> Settings.System.putString(cr, e.key, value)
            "secure" -> Settings.Secure.putString(cr, e.key, value)
            "global" -> Settings.Global.putString(cr, e.key, value)
            else -> false
        }
    }

    /** @return true if every entry was written and reads back as expected. */
    fun apply(profile: Profile, mute: Boolean): Boolean = try {
        profile.entries.all { e ->
            val v = if (mute) e.mute else e.restore
            write(e, v) && read(e) == v
        }
    } catch (_: SecurityException) {
        false
    }

    fun isMuted(profile: Profile) = profile.entries.isNotEmpty() && profile.entries.all { read(it) == it.mute }
}
