package com.example

import com.example.util.Formatters
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCurrencyParsing() {
    assertEquals(1250.50, Formatters.parseCurrencyInput("1.250,50"), 0.001)
    assertEquals(1250.50, Formatters.parseCurrencyInput("R$ 1.250,50"), 0.001)
    assertEquals(100.0, Formatters.parseCurrencyInput("100"), 0.001)
    assertEquals(0.0, Formatters.parseCurrencyInput(""), 0.001)
  }

  @Test
  fun testCurrencyFormatting() {
    val formatted = Formatters.formatCurrency(1500.0)
    assertTrue(formatted.contains("1.500,00"))
  }
}
