package dev.noshutter

import android.media.MediaActionSound

/** AudioSystem force-use values (system/media audio-base.h; stable across releases). */
object ForceUse {
    const val FOR_SYSTEM = 4
    const val FORCE_NONE = 0
    const val FORCE_SYSTEM_ENFORCED = 11

    /** What AudioService itself would choose on this device. */
    fun deviceDefault(): Int =
        if (MediaActionSound.mustPlayShutterSound()) FORCE_SYSTEM_ENFORCED else FORCE_NONE
}
