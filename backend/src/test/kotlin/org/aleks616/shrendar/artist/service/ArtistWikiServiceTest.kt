package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ChineseZodiacSign
import org.aleks616.shrendar.artist.model.ZodiacSign
import org.aleks616.shrendar.band.model.ArtistBandsDto
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate

class ArtistWikiServiceTest {
    private val artistService=mock(ArtistService::class.java)
    private val countryRepository=mock(CountryRepository::class.java)
    private val bandsMemberRepository=mock(BandsMemberRepository::class.java)
    private val userArtistRepository=mock(UserArtistRepository::class.java)
    private lateinit var service:ArtistWikiService
    private lateinit var artist:Artist

    @BeforeEach
    fun setup() {
        service=ArtistWikiService(
            artistService,
            countryRepository,
            bandsMemberRepository,
            userArtistRepository
        )
        artist=Artist().apply {
            id=1
            name="James Hetfield"
            birthDate=LocalDate.of(1963,8,3)
            deathDate=null
            gender='M'
            country=1
            description="Metallica frontman"
            artistImageUrl="https://example.com/james.jpg"
        }
    }

    @Test
    fun `getByIdWiki should map living artist data and favorite state`() {
        `when`(artistService.getById(1)).thenReturn(artist)
        `when`(countryRepository.getCountryNameById(1)).thenReturn("USA")
        `when`(userArtistRepository.existsByArtistIdAndUser_Login(1,"tester")).thenReturn(true)
        `when`(bandsMemberRepository.findBandsByArtistId(1)).thenReturn(
            listOf(ArtistBandsDto(10,1,"James Hetfield",3,"Metallica","Vocals",1981,null,null))
        )

        val result=service.getByIdWiki(1,"tester")

        assertEquals("James Hetfield",result.name)
        assertEquals("Male",result.gender)
        assertEquals("USA",result.country)
        assertEquals(ZodiacSign.LEO,result.zodiacSign)
        assertEquals(ChineseZodiacSign.RABBIT,result.chineseZodiacSign)
        assertEquals(artist.birthDate!!.until(LocalDate.now()).years,result.age)
        assertNull(result.deathDate)
        assertNull(result.daysTillDeathAnniversary)
        assertEquals(true,result.favorite)
        assertEquals(1,result.bands?.size)
        assertEquals(10,result.bands?.single()?.memberId)
        assertEquals(listOf("Vocals (1981-)"),result.bands?.single()?.yearRole)
    }

    @Test
    fun `getByIdWiki should calculate dead artist age and unknown gender`() {
        artist.deathDate=LocalDate.of(2020,9,27)
        artist.gender=null
        `when`(artistService.getById(1)).thenReturn(artist)
        `when`(countryRepository.getCountryNameById(1)).thenReturn("USA")
        `when`(bandsMemberRepository.findBandsByArtistId(1)).thenReturn(emptyList())

        val result=service.getByIdWiki(1,"tester")

        assertEquals(57,result.age)
        assertEquals("Unknown",result.gender)
        assertTrue(result.daysTillDeathAnniversary != null)
        assertFalse(result.favorite == true)
    }

    @Test
    fun `getBandsByArtistId should group roles by band`() {
        val first=ArtistBandsDto(10,2,"James Hetfield",3,"Metallica","Vocals",1981,null,null)
        val second=ArtistBandsDto(12,2,"James Hetfield",3,"Metallica","Guitar",1985,1990,null)
        val third=ArtistBandsDto(13,2,"James Hetfield",4,"Covers","Bass",1996,null,"J")
        `when`(bandsMemberRepository.findBandsByArtistId(2)).thenReturn(listOf(first,second,third))

        val result=service.getBandsByArtistId(2)

        assertEquals(2,result.size)
        assertEquals(10,result.first().memberId)
        assertEquals(3,result.first().bandId)
        assertEquals(listOf("Vocals (1981-)", "Guitar (1985-1990)"),result.first().yearRole)
        assertEquals(13,result.last().memberId)
        assertEquals("J",result.last().nickname)
    }

    @Test
    fun `getBandsByArtistId should return empty list for empty repository`() {
        `when`(bandsMemberRepository.findBandsByArtistId(2)).thenReturn(emptyList())

        assertTrue(service.getBandsByArtistId(2).isEmpty())
    }

    @Test
    fun `getBandsByArtistId should format same-year roles`() {
        val sameYear=ArtistBandsDto(10,2,"James Hetfield",3,"Metallica","Vocals",1981,1981,null)
        `when`(bandsMemberRepository.findBandsByArtistId(2)).thenReturn(listOf(sameYear))

        assertEquals(listOf("1981"),service.getBandsByArtistId(2).first().yearRole)
    }

    @Test
    fun `getZodiacSign should return every zodiac sign`() {
        val expectedByDate=mapOf(
            (12 to 22) to ZodiacSign.CAPRICORN,
            (1 to 1) to ZodiacSign.CAPRICORN,
            (1 to 20) to ZodiacSign.AQUARIUS,
            (2 to 1) to ZodiacSign.AQUARIUS,
            (2 to 18) to ZodiacSign.PISCES,
            (3 to 1) to ZodiacSign.PISCES,
            (3 to 20) to ZodiacSign.ARIES,
            (4 to 1) to ZodiacSign.ARIES,
            (4 to 20) to ZodiacSign.TAURUS,
            (5 to 1) to ZodiacSign.TAURUS,
            (5 to 21) to ZodiacSign.GEMINI,
            (6 to 1) to ZodiacSign.GEMINI,
            (6 to 21) to ZodiacSign.CANCER,
            (7 to 1) to ZodiacSign.CANCER,
            (7 to 23) to ZodiacSign.LEO,
            (8 to 1) to ZodiacSign.LEO,
            (8 to 23) to ZodiacSign.VIRGO,
            (9 to 1) to ZodiacSign.VIRGO,
            (9 to 23) to ZodiacSign.LIBRA,
            (10 to 1) to ZodiacSign.LIBRA,
            (10 to 23) to ZodiacSign.SCORPIO,
            (11 to 1) to ZodiacSign.SCORPIO,
            (11 to 22) to ZodiacSign.SAGITTARIUS,
            (12 to 1) to ZodiacSign.SAGITTARIUS
        )

        expectedByDate.forEach { (date,expected) ->
            assertEquals(expected,service.getZodiacSign(date.first,date.second))
        }
    }

    @Test
    fun `getZodiacSign should throw IllegalArgumentException for invalid date`() {
        assertThrows<IllegalArgumentException> {service.getZodiacSign(13,1)}
    }

    @Test
    fun `getChineseZodiacSign should return every zodiac sign`() {
        val expectedByYear=mapOf(
            1984 to ChineseZodiacSign.RAT,
            1985 to ChineseZodiacSign.OX,
            1986 to ChineseZodiacSign.TIGER,
            1987 to ChineseZodiacSign.RABBIT,
            1988 to ChineseZodiacSign.DRAGON,
            1989 to ChineseZodiacSign.SNAKE,
            1990 to ChineseZodiacSign.HORSE,
            1991 to ChineseZodiacSign.GOAT,
            1992 to ChineseZodiacSign.MONKEY,
            1993 to ChineseZodiacSign.ROOSTER,
            1994 to ChineseZodiacSign.DOG,
            1995 to ChineseZodiacSign.PIG,
        )

        expectedByYear.forEach { (year,expected) ->
            assertEquals(expected,service.getChineseZodiacSign(year))
        }
    }

    @Test
    fun `getChineseZodiacSign should throw IllegalArgumentException for invalid year`() {
        assertThrows<IllegalArgumentException> {service.getChineseZodiacSign(-5)}
    }
}
