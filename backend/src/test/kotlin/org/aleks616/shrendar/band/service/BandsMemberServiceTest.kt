package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class BandsMemberServiceTest {
    private val artistService=mock(ArtistService::class.java)
    private val bandService=mock(BandService::class.java)
    private val repository=mock(BandsMemberRepository::class.java)
    private val contributionRepository=mock(ContributionRepository::class.java)
    private val userService=mock(UserService::class.java)
    private val rankService=mock(RankService::class.java)
    private lateinit var bandsMemberService:BandsMemberService
    private lateinit var user:User
    private lateinit var member:BandsMembers

    @BeforeEach
    fun setup() {
        bandsMemberService=BandsMemberService(
            artistService,bandService,repository,contributionRepository,userService,rankService
        )
        user=User().apply {id=7; login="user"; rank=Rank().apply {id=1}}
        member=BandsMembers().apply {
            id=10
            artist=Artist().apply {id=2; name="James"}
            band=Band().apply {id=3; name="Metallica"}
            role="Vocals"
            joinedYear=1981
            nickname="Het"
        }
    }

    @Test
    fun `doesBandMemberExist should return true for existing member`() {
        `when`(repository.existsById(10L)).thenReturn(true)
        assertTrue(bandsMemberService.doesBandMemberExist(10))
    }

    @Test
    fun `getBandMembersRaw should return repository rows`() {
        val raw=BandsMembersDataDto(10,2,"James",3,"Metallica","Vocals",1981,null,"Het")
        `when`(repository.findAllByBandName(3)).thenReturn(listOf(raw))
        assertEquals(listOf(raw),bandsMemberService.getBandMembersRaw(3))
    }

    @Test
    fun `getAllBandMembers should group roles for one artist`() {
        val first=BandsMembersDataDto(10,2,"James",3,"Metallica","Vocals",1981,null,"Het")
        val second=BandsMembersDataDto(11,2,"James",3,"Metallica","Guitar",1985,1990,"Het")
        `when`(repository.findAllByBandName(3)).thenReturn(listOf(first,second))

        val all=bandsMemberService.getAllBandMembers(3)
        assertEquals(1,all.size)
        assertEquals(listOf("Vocals (1981-)", "Guitar (1985-1990)"),all.first().yearRole)
    }

    @Test
    fun `getAllBandMembersWiki should map member data`() {
        val source=BandsMembersDto(10,2,"James",3,"Metallica","Het",mutableListOf("Vocals (1981-)"))
        `when`(repository.findAllByBandName(3)).thenReturn(
            listOf(BandsMembersDataDto(10,2,"James",3,"Metallica","Vocals",1981,null,"Het"))
        )
        assertEquals(
            listOf(BandsMembersWikiDto(source.id,source.artistId,source.artistName,source.bandId,source.nickname,source.yearRole)),
            bandsMemberService.getAllBandMembersWiki(3)
        )
    }

    @Test
    fun `getCurrentBandMembers should return open ended roles`() {
        val current=BandsMembersDataDto(10,2,"James",3,"Metallica","Vocals",1981,null,null)
        val past=BandsMembersDataDto(11,4,"Lars",3,"Metallica","Drums",1981,1990,null)
        `when`(repository.findAllByBandName(3)).thenReturn(listOf(current,past))
        assertEquals(listOf(2L),bandsMemberService.getCurrentBandMembers(3).map {it.artistId})
    }

    @Test
    fun `getPastBandMembers should exclude open ended roles`() {
        val current=BandsMembersDataDto(10,2,"James",3,"Metallica","Vocals",1981,null,null)
        val past=BandsMembersDataDto(11,4,"Lars",3,"Metallica","Drums",1981,1990,null)
        `when`(repository.findAllByBandName(3)).thenReturn(listOf(current,past))
        assertEquals(listOf(4L),bandsMemberService.getPastBandMembers(3).map {it.artistId})
    }

    @Test
    fun `getBandsByArtistId should group roles`() {
        val first=ArtistBandsDto(10,2,"James",3,"Metallica","Vocals",1981,null,"Het")
        val second=ArtistBandsDto(12,2,"James",3,"Metallica","Guitar",1985,1990,"Het")
        `when`(repository.findBandsByArtistId(2)).thenReturn(listOf(first,second))

        val result=bandsMemberService.getBandsByArtistId(2)
        assertEquals(1,result.size)
        assertEquals(listOf("Vocals (1981-)", "Guitar (1985-1990)"),result.first().yearRole)
    }

    @Test
    fun `addBandMemberRequest should throw ContributionLimitExceededException when user reaches contribution limit`() {
        val dto=ArtistBandAddDto(artistId=2,bandId=3,role="Vocals",joinedYear=1981)
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {bandsMemberService.addBandMemberRequest(dto,"user")}
        verifyNoInteractions(repository)

    }

    @Test
    fun `addBandMemberRequest should record member contribution`() {
        val dto=ArtistBandAddDto(artistId=2,bandId=3,role="Vocals",joinedYear=1981)
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(artistService.getById(2)).thenReturn(member.artist!!)
        `when`(bandService.getBandById(3)).thenReturn(member.band!!)
        `when`(repository.findTopIdByBandIdAndArtistId(3,2)).thenReturn(10)
        bandsMemberService.addBandMemberRequest(dto,"user")
        verify(repository).save(any(BandsMembers::class.java))
        verify(contributionRepository,atLeastOnce()).save(any(Contribution::class.java))
    }

    @Test
    fun `addBandMemberRequest should confirm contributions for rank nine`() {
        user.rank=Rank().apply {id=9}
        val dto=ArtistBandAddDto(artistId=2,bandId=3,role="Vocals",joinedYear=1981)
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(artistService.getById(2)).thenReturn(member.artist!!)
        `when`(bandService.getBandById(3)).thenReturn(member.band!!)
        `when`(repository.findTopIdByBandIdAndArtistId(3,2)).thenReturn(10)

        bandsMemberService.addBandMemberRequest(dto,"user")

        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(captor.capture())
        assertTrue(captor.allValues.all {it.confirmed==true&&it.confirmedBy==7})
    }

    @Test
    fun `editBandMemberRequest should update changed fields`() {
        val dto=ArtistBandAddDto(10,3,2,"New","Guitar",1982,1989)
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(repository.findById(10L)).thenReturn(member)
        `when`(bandService.getBandById(3)).thenReturn(member.band!!)
        `when`(artistService.getById(2)).thenReturn(member.artist!!)
        bandsMemberService.editBandMemberRequest(dto,"user")
        verify(repository).save(member)
        verify(contributionRepository,atLeastOnce()).save(any(Contribution::class.java))

    }

    @Test
    fun `editBandMemberRequest should throw IllegalStateException when there are no changes`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(repository.findById(10L)).thenReturn(member)
        assertThrows<IllegalStateException> {
            bandsMemberService.editBandMemberRequest(ArtistBandAddDto(10),"user")
        }
    }

    @Test
    fun `getAllBandMembers should return empty list for empty repository`() {
        `when`(repository.findAllByBandName(3)).thenReturn(emptyList())
        assertTrue(bandsMemberService.getAllBandMembers(3).isEmpty())
    }

    @Test
    fun `getAllBandMembers should format same-year roles without range`() {
        val sameYear=BandsMembersDataDto(12,4,"Lars",3,"Metallica","Drums",1990,1990,null)
        `when`(repository.findAllByBandName(3)).thenReturn(listOf(sameYear))
        assertEquals(listOf("Drums (1990)"),bandsMemberService.getAllBandMembers(3).first().yearRole)
    }

    @Test
    fun `getBandsByArtistId should return empty list for empty repository`() {
        `when`(repository.findBandsByArtistId(2)).thenReturn(emptyList())
        assertTrue(bandsMemberService.getBandsByArtistId(2).isEmpty())
    }

    @Test
    fun `getBandsByArtistId should format same-year roles`() {
        val sameYear=ArtistBandsDto(
            10,2,"James",3,"Metallica","Vocals",1981,1981,"Het"
        )
        `when`(repository.findBandsByArtistId(2)).thenReturn(listOf(sameYear))
        assertEquals(listOf("1981"),bandsMemberService.getBandsByArtistId(2).first().yearRole)
    }

    @Test
    fun `addBandMemberRequest should omit null member fields from contributions`() {
        user.rank=Rank().apply {id=8}
        val dto=ArtistBandAddDto(artistId=2,bandId=3,role=null,joinedYear=1981,leftYear=null,nickname=null)
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(artistService.getById(2)).thenReturn(member.artist!!)
        `when`(bandService.getBandById(3)).thenReturn(member.band!!)
        `when`(repository.findTopIdByBandIdAndArtistId(3,2)).thenReturn(10)
        `when`(contributionRepository.findTopChangeId()).thenReturn(5)

        bandsMemberService.addBandMemberRequest(dto,"user")
        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(captor.capture())
        assertTrue(captor.allValues.none {it.changedColumn=="role"||it.changedColumn=="nickname"})
    }

    @Test
    fun `editBandMemberRequest should throw ContributionLimitExceededException when user reaches contribution limit`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            bandsMemberService.editBandMemberRequest(ArtistBandAddDto(10,joinedYear=2000),"user")
        }
        verifyNoInteractions(repository)

    }

    @Test
    fun `editBandMemberRequest should throw IllegalArgumentException when joined year is after existing left year`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        member.leftYear=1990
        `when`(repository.findById(10L)).thenReturn(member)
        assertThrows<IllegalArgumentException> {
            bandsMemberService.editBandMemberRequest(ArtistBandAddDto(10,joinedYear=2000,leftYear=2001),"user")
        }
    }

    @Test
    fun `editBandMemberRequest should throw IllegalArgumentException when left year is below existing joined year`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(repository.findById(10L)).thenReturn(member)

        assertThrows<IllegalArgumentException> {
            bandsMemberService.editBandMemberRequest(ArtistBandAddDto(10,leftYear=1980),"user")
        }
    }

    @Test
    fun `editBandMemberRequest should update every supported field`() {
        user.rank=Rank().apply {id=10}
        val dto=ArtistBandAddDto(10,4,5,"New","Guitar",1982,1989)
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(repository.findById(10L)).thenReturn(member)
        `when`(bandService.getBandById(4)).thenReturn(Band().apply {id=4})
        `when`(artistService.getById(5)).thenReturn(Artist().apply {id=5})
        bandsMemberService.editBandMemberRequest(dto,"user")

        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(captor.capture())
        assertEquals(setOf("band_id","artist_id","nickname","role","joined_year","left_year"),
            captor.allValues.map {it.changedColumn}.toSet())
    }

    @Test
    fun `editBandMemberRequest should mark contributions as trusted for rank ten`() {
        user.rank=Rank().apply {id=10}
        val dto=ArtistBandAddDto(10,nickname="New")
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(repository.findById(10L)).thenReturn(member)
        bandsMemberService.editBandMemberRequest(dto,"user")
        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository).save(captor.capture())
        assertTrue(captor.allValues.all {it.confirmed==true&&it.confirmedBy==7})
    }

    @Test
    fun `deleteBandMemberRequest should throw ContributionLimitExceededException when user reaches contribution limit`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            bandsMemberService.deleteBandMemberRequest(10,"user")
        }
        verifyNoInteractions(repository,contributionRepository)

    }

    @Test
    fun `deleteBandMemberRequest should skip contributions when logging is disabled`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        bandsMemberService.deleteBandMemberRequest(10,"user",log=false)
        verifyNoInteractions(contributionRepository)
        verify(repository,never()).deleteById(10L)
    }

    @Test
    fun `deleteBandMemberRequest should remove member for rank ten`() {
        user.rank=Rank().apply {id=10}
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)

        bandsMemberService.deleteBandMemberRequest(10,"user",log=false)

        verify(repository).deleteById(10L)
    }

    @Test
    fun `deleteBandMemberRequest should log every member field`() {
        `when`(userService.getUserByLogin("user")).thenReturn(user)
        `when`(rankService.checkRank(user)).thenReturn(null)
        `when`(repository.findById(10L)).thenReturn(member)
        `when`(contributionRepository.findTopChangeId()).thenReturn(20)

        bandsMemberService.deleteBandMemberRequest(10,"user",log=true)

        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,times(7)).save(captor.capture())
        val byColumn=captor.allValues.associateBy {it.changedColumn}
        assertEquals(setOf("id","band_id","artist_id","nickname","role","joined_year","left_year"),byColumn.keys)
        assertEquals("10",byColumn["id"]?.oldValue)
        assertEquals("3",byColumn["band_id"]?.oldValue)
        assertEquals("2",byColumn["artist_id"]?.oldValue)
        assertEquals("Het",byColumn["nickname"]?.oldValue)
        assertEquals("Vocals",byColumn["role"]?.oldValue)
        assertEquals("1981",byColumn["joined_year"]?.oldValue)
        assertEquals("null",byColumn["left_year"]?.oldValue)
        assertTrue(captor.allValues.all {
            it.action==Action.DELETE&&it.changedTable=="bands_members"&&
                it.changedRecordId==10L&&it.newValue==null&&it.confirmed==false&&it.confirmedBy==null
        })
        verify(repository,never()).deleteById(10L)
    }
}
