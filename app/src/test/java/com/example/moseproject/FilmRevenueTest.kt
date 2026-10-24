package com.example.moseproject

import com.example.moseproject.data.model.FilmIdResult
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class FilmRevenueTest {
    @Test
    fun filmDetailsSupportRevenueAboveTheIntegerLimit() {
        val film = Gson().fromJson(
            """{"id":1,"title":"Película","budget":300000000,"revenue":2516291000}""",
            FilmIdResult::class.java
        )
        assertEquals(2_516_291_000L, film.revenue)
        assertEquals(300_000_000L, film.budget)
    }
}
