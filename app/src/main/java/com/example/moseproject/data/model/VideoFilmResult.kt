package com.example.moseproject.data.model

data class VideoResult(
    val id: Int,
    val results: List<ResultVideo>
)

data class ResultVideo(
    val id: String,
    val iso_3166_1: String,
    val iso_639_1: String,
    val key: String,
    val name: String,
    val official: Boolean,
    val published_at: String,
    val site: String,
    val size: Int,
    val type: String
)

// Keep the Spanish-only behaviour, including when TMDB returns other providers.
fun VideoResult.spanishYouTubeVideos(): VideoResult = copy(
    results = results.filter {
        it.iso_639_1.equals("es", ignoreCase = true) &&
            it.site.equals("YouTube", ignoreCase = true) &&
            it.key.matches(Regex("[A-Za-z0-9_-]{11}"))
    }.sortedWith(
        compareByDescending<ResultVideo> { it.type == "Trailer" }
            .thenByDescending { it.official }
    )
)
