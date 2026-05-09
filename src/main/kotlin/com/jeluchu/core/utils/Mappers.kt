@file:Suppress("unused")

package com.jeluchu.core.utils

import com.jeluchu.core.extensions.toYouTubeWatchUrl
import com.jeluchu.core.models.jikan.anime.ImageFormat
import com.jeluchu.core.models.jikan.anime.Trailer
import com.jeluchu.features.anime.models.anime.Images
import com.jeluchu.features.anime.models.anime.VideoPromo

fun Trailer.toVideoPromo() = VideoPromo(
    url = url.orEmpty(),
    youtubeId = youtubeId.orEmpty(),
    embedUrl = embedUrl.toYouTubeWatchUrl().orEmpty(),
    images = images?.toImages() ?: Images()
)

fun ImageFormat.toImages() = Images(
    generic = generic.orEmpty(),
    small = small.orEmpty(),
    medium = medium.orEmpty(),
    large = large.orEmpty(),
    maximum = maximum.orEmpty()
)