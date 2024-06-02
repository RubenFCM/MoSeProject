package com.example.moseproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.moseproject.data.ApiService
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.PersonResult
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.ui.viewmodel.PersonIDViewModel
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import kotlin.test.assertEquals
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Rule

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

}

@ExperimentalCoroutinesApi
class PersonIDViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var viewModel: PersonIDViewModel
    private lateinit var service: ApiService


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        service = mockk<ApiService>()

        // Mock the static method
        println("Simulando el método estático de RetrofitServiceFactory")
        mockkObject(RetrofitServiceFactory)
        every { RetrofitServiceFactory.makeRetrofitServiceFactory() } returns service

        println("Creando instancia de PersonIDViewModel")
        viewModel = PersonIDViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        unmockkAll()
    }

    @Test
    fun `getPerson should update dataPerson with person result`() = runBlocking {
        val personResult = PersonResult(
            false,
            listOf("Jenna Marie Ortega", "珍娜·奧特嘉", "珍娜·奥尔特加", "ジェナ・オルテガ", "Дженна Ортега", "جنا اورتگا", "제나 오르테가"),
            "Jenna Marie Ortega (born September 27, 2002) is an American actress...",
            "2002-09-27",
            null,
            1,
            null,
            974169,
            "nm4911194",
            "Acting",
            "Jenna Ortega",
            "Coachella Valley, Palm Desert, California, USA",
            316.64,
            "/7oUAtVgZU0uLdUSvDHKkINt1y7Y.jpg"
        )
        val personResult1 = PersonResult(
            false,
            listOf("Jenna Marie Ortega", "珍娜·奧特嘉", "珍娜·奥尔特加", "ジェナ・オルテガ", "Дженна Ортега", "جنا اورتگا", "제나 오르테가"),
            "Jenna Marie Ortega (born September 27, 2002) is an American actress...",
            "2002-09-27",
            null,
            1,
            null,
            974169,
            "nm4911194",
            "Acting",
            "Jenna Ortega",
            "Coachella Valley, Palm Desert, California, USA",
            316.64,
            "/7oUAtVgZU0uLdUSvDHKkINt1y7Y.jpg2222"
        )
        coEvery { service.searchPerson(any(), any()) } returns personResult

        viewModel.getPerson("974169")

        val result = viewModel.dataPerson.value
        assertEquals(personResult1, result)
    }

    @Test
    fun `getPerson should handle exception`() = runBlocking {
        coEvery { service.searchPerson("12345", Constants.API_KEY) } throws Exception("Network error")

        println("Ejecutando prueba getPerson")
        viewModel.getPerson("12345")

        val result = viewModel.dataPerson.first()
        assertEquals(null, result)

        coVerify { service.searchPerson("12345", Constants.API_KEY) }
    }
}