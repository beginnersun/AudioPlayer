package com.example.audioplayer

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val list1 = mutableListOf("1","2","3","4","5","4","4")
        println(list1.size)
        val list2 = list1.filter { it == "4" }
        println(list1.size)
        println(list2.size)

    }
}
