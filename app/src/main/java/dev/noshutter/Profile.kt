package dev.noshutter

import android.content.Context
import android.os.Build
import org.json.JSONObject

data class SettingEntry(val table: String, val key: String, val mute: String, val restore: String)

data class Profile(
    val id: String,
    val name: String,
    val manufacturers: List<String>,
    val models: List<String>,
    val verified: Boolean,
    val entries: List<SettingEntry>,
) {
    fun matchesThisDevice(): Boolean {
        val maker = (Build.MANUFACTURER + " " + Build.BRAND).lowercase()
        val model = Build.MODEL.lowercase()
        val makerOk = manufacturers.isEmpty() || manufacturers.any { maker.contains(it) }
        val modelOk = models.isEmpty() || models.any { model.contains(it) }
        return makerOk && modelOk
    }

    companion object {
        fun loadAll(context: Context): List<Profile> {
            val text = context.assets.open("profiles.json").bufferedReader().use { it.readText() }
            val arr = JSONObject(text).getJSONArray("profiles")
            return (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val match = o.optJSONObject("match")
                fun list(name: String) = match?.optJSONArray(name)?.let { a ->
                    (0 until a.length()).map { a.getString(it).lowercase() }
                } ?: emptyList()
                val es = o.getJSONArray("entries")
                Profile(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    manufacturers = list("manufacturer"),
                    models = list("model"),
                    verified = o.optBoolean("verified", false),
                    entries = (0 until es.length()).map {
                        val e = es.getJSONObject(it)
                        SettingEntry(e.getString("table"), e.getString("key"), e.getString("mute"), e.getString("restore"))
                    },
                )
            }
        }
    }
}
