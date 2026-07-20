package org.aleks616.shrendar.album.controller

import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.AlbumDataDto
import org.aleks616.shrendar.album.service.AlbumService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class AlbumControllerTest {

    private val albumService:AlbumService=mock(AlbumService::class.java)
    private val controller=AlbumController(albumService)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `getAlbum should return all albums`() {
        val albums=listOf(AlbumDataDto(id=1,title="Album 1"))
        `when`(albumService.getAll()).thenReturn(albums)

        mockMvc.get("/api/album/")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'title':'Album 1'}]")}
            }
    }

    @Test
    fun `getAlbumAnniversariesByDate should return albums for valid date`() {
        val albums=listOf(AlbumByDateDto(id=1,title="Anniversary"))
        `when`(albumService.getAlbumAnniversariesByDate(5,20)).thenReturn(albums)

        mockMvc.get("/api/album/inDate") {
            param("month","5")
            param("day","20")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'title':'Anniversary'}]")}
        }
    }

    @Test
    fun `getAlbumAnniversariesByDate should throw error for invalid date`() {
        val ex=assertThrows<IllegalArgumentException> {
            controller.getAlbumAnniversariesByDate(13,1)
        }
        assertEquals("Invalid date",ex.message)
    }

    @Test
    fun `getAlbumsByBandId should return albums for existing band`() {
        val albums=listOf(AlbumDataDto(id=1,title="Band Album"))
        `when`(albumService.doesBandExist(1)).thenReturn(true)
        `when`(albumService.getAlbumsByBandId(1)).thenReturn(albums)

        mockMvc.get("/api/album/band/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'title':'Band Album'}]")}
            }
    }

    @Test
    fun `getAlbumsByBandId should throw error for non-existent band`() {
        `when`(albumService.doesBandExist(999)).thenReturn(false)
        val ex=assertThrows<IllegalArgumentException> {
            controller.getAlbumsByBandId(999)
        }
        assertEquals("Band doesn't exist",ex.message)
    }

    @Test
    fun `getAlbumsByBandNameLike should return albums`() {
        val albums=listOf(AlbumDataDto(id=1,title="Some Album"))
        `when`(albumService.getAlbumsByBandName("Metallica")).thenReturn(albums)

        mockMvc.get("/api/album/band/like/Metallica")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'title':'Some Album'}]")}
            }
    }

    @Test
    fun `getAlbumsByYear should return albums for valid year`() {
        val currentYear=LocalDate.now().year
        val albums=listOf(AlbumDataDto(id=1,title="Year Album"))
        `when`(albumService.getAlbumsByYear(2020)).thenReturn(albums)

        mockMvc.get("/api/album/year/2020").andExpect {status {isOk()}}
        mockMvc.get("/api/album/year/1918").andExpect {status {isOk()}}
        mockMvc.get("/api/album/year/$currentYear").andExpect {status {isOk()}}
    }

    @Test
    fun `getAlbumsByYear should throw error for invalid year`() {
        val futureYear=LocalDate.now().year+1

        assertEquals("Invalid year",assertThrows<IllegalArgumentException> {
            controller.getAlbumsByYear(1917)
        }.message)

        assertEquals("Invalid year",assertThrows<IllegalArgumentException> {
            controller.getAlbumsByYear(futureYear)
        }.message)
    }

    @Test
    fun `getAlbumsByNameLike should return albums`() {
        val albums=listOf(AlbumDataDto(id=1,title="Master of Puppets"))
        `when`(albumService.getAlbumsByName("Master")).thenReturn(albums)

        mockMvc.get("/api/album/like/Master")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'title':'Master of Puppets'}]")}
            }
    }

    @Test
    fun `getAlbumsByNameExact should return albums`() {
        val albums=listOf(AlbumDataDto(id=1,title="Master of Puppets"))
        `when`(albumService.getAlbumsByNameExact("Master of Puppets")).thenReturn(albums)

        mockMvc.get("/api/album/exact/Master of Puppets")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'title':'Master of Puppets'}]")}
            }
    }
}
