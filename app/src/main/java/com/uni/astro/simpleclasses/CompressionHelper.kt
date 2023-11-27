package com.uni.astro.simpleclasses

import android.content.Context
import android.net.Uri
import com.uni.astro.compressionmodule.CompressionListener
import com.uni.astro.compressionmodule.VideoCompressor
import com.uni.astro.compressionmodule.VideoQuality
import com.uni.astro.compressionmodule.config.Configuration
import com.uni.astro.compressionmodule.config.SharedStorageConfiguration

object CompressionHelper {

    public fun processVideo(context: Context, uris:List<Uri>,listener: CompressionListener) {
        VideoCompressor.start(
                context,
                uris,
                isStreamable = false,
                sharedStorageConfiguration = SharedStorageConfiguration(
                        subFolderName = "${(Variables.APP_HIDED_RESULT_FOLDER).replace("/","")}"
                ),
                configureWith = Configuration(
                        quality = VideoQuality.MEDIUM,
                        videoNames = uris.map { uri -> uri.pathSegments.last() },
                        isMinBitrateCheckEnabled = true,
                ),
                listener = listener,
        )
    }
}