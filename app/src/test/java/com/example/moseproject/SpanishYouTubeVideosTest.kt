package com.example.moseproject

import com.example.moseproject.data.model.ResultVideo
import com.example.moseproject.data.model.VideoResult
import com.example.moseproject.data.model.spanishYouTubeVideos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpanishYouTubeVideosTest {
    private fun video(
        language: String = "es",
        site: String = "YouTube",
        key: String = "abcdefghijk",
        type: String = "Trailer",
        official: Boolean = true
    ) = ResultVideo("id", "ES", language, key, "Video", official, "", site, 1080, type)

    @Test
    fun noSpanishVideoLeavesThePlayerHidden() {
        val result = VideoResult(1, listOf(video(language = "en")))
        assertTrue(result.spanishYouTubeVideos().results.isEmpty())
        assertTrue(VideoResult(1, emptyList()).spanishYouTubeVideos().results.isEmpty())
    }

    @Test
    fun onlyValidSpanishYouTubeVideosAreSelected() {
        val spanish = video()
        val result = VideoResult(1, listOf(
            video(language = "en"), video(site = "Vimeo"), video(key = ""), spanish
        ))
        assertEquals(listOf(spanish), result.spanishYouTubeVideos().results)
    }

    @Test
    fun officialTrailerIsPreferredToOtherSpanishVideos() {
        val trailer = video(key = "12345678901")
        val result = VideoResult(1, listOf(
            video(type = "Teaser"), video(official = false), trailer
        ))
        assertEquals(trailer, result.spanishYouTubeVideos().results.first())
    }
}
