package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.ChineseZodiacSign
import org.aleks616.shrendar.artist.model.ZodiacSign
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

class ArtistServiceTest {

    private val artistRepository=mock(ArtistRepository::class.java)
    private val countryRepository=mock(CountryRepository::class.java)
    private val artistService=ArtistService(artistRepository,countryRepository)

    @Test
    fun `getZodiacSign should return correct signs`() {
        assertEquals(ZodiacSign.CAPRICORN,artistService.getZodiacSign(1,10))
        assertEquals(ZodiacSign.AQUARIUS,artistService.getZodiacSign(1,25))
        assertEquals(ZodiacSign.PISCES,artistService.getZodiacSign(2,20))
        assertEquals(ZodiacSign.ARIES,artistService.getZodiacSign(3,25))
        assertEquals(ZodiacSign.TAURUS,artistService.getZodiacSign(4,25))
        assertEquals(ZodiacSign.GEMINI,artistService.getZodiacSign(5,25))
        assertEquals(ZodiacSign.CANCER,artistService.getZodiacSign(6,25))
        assertEquals(ZodiacSign.LEO,artistService.getZodiacSign(7,25))
        assertEquals(ZodiacSign.VIRGO,artistService.getZodiacSign(8,25))
        assertEquals(ZodiacSign.LIBRA,artistService.getZodiacSign(9,25))
        assertEquals(ZodiacSign.SCORPIO,artistService.getZodiacSign(10,25))
        assertEquals(ZodiacSign.SAGITTARIUS,artistService.getZodiacSign(11,25))
        assertEquals(ZodiacSign.CAPRICORN,artistService.getZodiacSign(12,25))
    }

    @Test
    fun `getZodiacSign should throw error for invalid date`() {
        assertThrows<IllegalArgumentException> {
            artistService.getZodiacSign(13,1)
        }
    }

    @Test
    fun `getChineseZodiacSign should return correct signs`() {
        assertEquals(ChineseZodiacSign.MONKEY,artistService.getChineseZodiacSign(1980))
        assertEquals(ChineseZodiacSign.ROOSTER,artistService.getChineseZodiacSign(1981))
        assertEquals(ChineseZodiacSign.DOG,artistService.getChineseZodiacSign(1982))
        assertEquals(ChineseZodiacSign.PIG,artistService.getChineseZodiacSign(1983))
        assertEquals(ChineseZodiacSign.RAT,artistService.getChineseZodiacSign(1984))
        assertEquals(ChineseZodiacSign.OX,artistService.getChineseZodiacSign(1985))
        assertEquals(ChineseZodiacSign.TIGER,artistService.getChineseZodiacSign(1986))
        assertEquals(ChineseZodiacSign.RABBIT,artistService.getChineseZodiacSign(1987))
        assertEquals(ChineseZodiacSign.DRAGON,artistService.getChineseZodiacSign(1988))
        assertEquals(ChineseZodiacSign.SNAKE,artistService.getChineseZodiacSign(1989))
        assertEquals(ChineseZodiacSign.HORSE,artistService.getChineseZodiacSign(1990))
        assertEquals(ChineseZodiacSign.GOAT,artistService.getChineseZodiacSign(1991))
    }
}
