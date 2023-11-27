package com.uni.astro.compressionmodule.config

import com.uni.astro.compressionmodule.VideoQuality


data class Configuration(
        var quality: VideoQuality = VideoQuality.MEDIUM,
        var isMinBitrateCheckEnabled: Boolean = true,
        var videoBitrateInMbps: Int? = null,
        var disableAudio: Boolean = false,
        val keepOriginalResolution: Boolean = false,
        var videoHeight: Double? = null,
        var videoWidth: Double? = null,
        var videoNames: List<String>
)

data class AppSpecificStorageConfiguration(
    var subFolderName: String? = null,
)

data class SharedStorageConfiguration(
    var subFolderName: String? = null,
)
