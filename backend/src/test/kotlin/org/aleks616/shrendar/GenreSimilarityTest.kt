package org.aleks616.shrendar

import org.aleks616.shrendar.GenreSimilarity.getGenreSimilarity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class GenreSimilarityTest {

    @Test
    fun getGenreSimilarityTest(){
        val genre1="3540000"
        val genre2="2440010"
        val expected=2.67
        assertEquals(expected,getGenreSimilarity(genre1,genre2))

    }
}