package com.yashovardhan99.core.backup_restore

import com.google.common.truth.Truth.assertThat
import java.io.BufferedReader
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CsvReaderTest {
    private lateinit var bufferedReader: BufferedReader
    private lateinit var csvReader: CsvReader

    @Before
    fun setup() {
        bufferedReader = mock(BufferedReader::class.java)
        `when`(bufferedReader.readLine()).thenReturn(
            "Item1,Item2,Item3",
            "-6,5.67,6",
            "\"\"\"Test\",Test2,\"Test3,\"",
            null
        )
        csvReader = CsvReader(bufferedReader)
    }

    @Test
    fun parseRows() {
        runBlocking {
            val line1 = csvReader.parseRow()
            assertThat(line1).containsExactly("Item1", "Item2", "Item3")
            val line2 = csvReader.parseRow()
            assertThat(line2).containsExactly("-6", "5.67", "6")
            val line3 = csvReader.parseRow()
            assertThat(line3).containsExactly("\"Test", "Test2", "Test3,")
            val line4 = csvReader.parseRow()
            assertThat(line4).isEmpty()
            Mockito.verify(bufferedReader, times(4)).readLine()
        }
    }

    @Test
    fun close() {
        runBlocking {
            csvReader.close()
            Mockito.verify(bufferedReader).close()
        }
    }
}