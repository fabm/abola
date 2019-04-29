package pt.fabm

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pt.fabm.abola.extensions.passMatches
import pt.fabm.abola.extensions.toHash

class StringExtensionTest {

  @Test
  fun testHashLoader(){
    val hash = "hello extensions".toHash()
    Assertions.assertTrue("hello extensions" passMatches hash)
    Assertions.assertFalse("another extensions" passMatches hash)
  }
}
