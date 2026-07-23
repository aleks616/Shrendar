package org.aleks616.shrendar.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtilsTest {

    @Test
    fun `doesDateExist should return true for valid dates`() {
        assertTrue(Utils.doesDateExist(1,1))
        assertTrue(Utils.doesDateExist(12,31))
        assertTrue(Utils.doesDateExist(2,29))
        assertTrue(Utils.doesDateExist(4,30))
    }

    @Test
    fun `doesDateExist should return false for invalid dates`() {
        assertFalse(Utils.doesDateExist(13,1))
        assertFalse(Utils.doesDateExist(1,32))
        assertFalse(Utils.doesDateExist(2,30))
        assertFalse(Utils.doesDateExist(4,31))
    }

    @Test
    fun `getDaysTillNextAnniversary should return 0 if today is anniversary`() {
        val today=LocalDate.now()
        assertEquals(0,Utils.getDaysTillNextAnniversary(today))
    }

    @Test
    fun `getDaysTillNextAnniversary should return positive days for future anniversary this year`() {
        val today=LocalDate.now()
        val future=today.plusDays(10)
        val days=Utils.getDaysTillNextAnniversary(future)
        assertEquals(10,Utils.getDaysTillNextAnniversary(LocalDate.of(1990,future.month,future.dayOfMonth)))
    }

    @Test
    fun `getDaysTillNextAnniversary should return days for anniversary next year`() {
        val today=LocalDate.now()
        val past=today.minusDays(10)
        val days=Utils.getDaysTillNextAnniversary(LocalDate.of(1990,past.month,past.dayOfMonth))
        assertTrue(days>350)
    }
}
