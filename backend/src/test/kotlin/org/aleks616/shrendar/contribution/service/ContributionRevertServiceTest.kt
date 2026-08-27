package org.aleks616.shrendar.contribution.service

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.RankTooLowToRevertConfirmedContributionException
import org.aleks616.shrendar.exception.RankTooLowToRevertContributionException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class ContributionRevertServiceTest {
    private lateinit var albumRepository:AlbumRepository
    private lateinit var artistRepository:ArtistRepository
    private lateinit var bandRepository:BandRepository
    private lateinit var bandService:BandService
    private lateinit var bandsMemberRepository:BandsMemberRepository
    private lateinit var contributionRepository:ContributionRepository
    private lateinit var userService:UserService
    private lateinit var service:ContributionRevertService
    private lateinit var user:User

    @BeforeEach
    fun setup() {
        albumRepository=mock(AlbumRepository::class.java)
        artistRepository=mock(ArtistRepository::class.java)
        bandRepository=mock(BandRepository::class.java)
        bandService=mock(BandService::class.java)
        bandsMemberRepository=mock(BandsMemberRepository::class.java)
        contributionRepository=mock(ContributionRepository::class.java)
        userService=mock(UserService::class.java)
        service=ContributionRevertService(
            albumRepository,artistRepository,bandRepository,bandService,
            bandsMemberRepository,contributionRepository,userService
        )
        user=User().apply {login="trusted"; rank=Rank().apply {id=12}}
        `when`(userService.getUserByLogin("trusted")).thenReturn(user)
    }

    @Test
    fun `revertAddition should throw RankTooLowToRevertContributionException for rank below 10`() {
        user.rank=Rank().apply {id=9}
        assertThrows<RankTooLowToRevertContributionException> {service.revertAddition(1,"trusted")}
        verifyNoInteractions(contributionRepository)
    }

    @Test
    fun `revertAddition should throw RankTooLowToRevertConfirmedContributionException for rank below 12`() {
        user.rank=Rank().apply {id=10}
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("artist",true)))
        assertThrows<RankTooLowToRevertConfirmedContributionException> {service.revertAddition(1,"trusted")}
    }

    @Test
    fun `revertAddition should throw RankTooLowToRevertConfirmedContributionException for rank 11`() {
        user.rank=Rank().apply {id=11}
        `when`(contributionRepository.getByChangeId(1)).thenReturn(
            listOf(contribution("artist",true).apply {changedRecordId=1})
        )
        assertThrows<RankTooLowToRevertConfirmedContributionException> {
            service.revertAddition(1,"trusted")
        }
        verifyNoInteractions(artistRepository,bandRepository,albumRepository,bandsMemberRepository)
    }

    @Test
    fun `revertAddition should throw UnsupportedOperationException for unsupported action`() {
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("artist",false,Action.UPDATE)))
        assertThrows<UnsupportedOperationException> {service.revertAddition(1,"trusted")}
    }

    @Test
    fun `revertAddition should throw IllegalArgumentException for unsupported table`() {
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("unknown",false)))
        assertThrows<IllegalArgumentException> {service.revertAddition(1,"trusted")}
    }

    @Test
    fun `revertAddition should delete album and recalculate genre for album addition`() {
        val album=Album().apply {id=4}
        `when`(contributionRepository.getByChangeId(1)).thenReturn(
            listOf(
            contribution("album",false).apply {changedRecordId=4},
            contribution("album",false).apply {changedColumn="band_id"; newValue="8"}
        ))
        `when`(albumRepository.findAlbumById(4)).thenReturn(album)
        service.revertAddition(1,"trusted")
        verify(albumRepository).delete(album)
        verify(bandService).calculateBandsGenre(8)
    }

    @Test
    fun `revertAlbumAddition should skip genre recalculation when band id is missing`() {
        val album=Album().apply {id=4}
        val item=contribution("album",false).apply {changedRecordId=4}
        `when`(albumRepository.findAlbumById(4)).thenReturn(album)
        service.revertAlbumAddition(listOf(item))
        verify(albumRepository).delete(album)
        verifyNoInteractions(bandService)
    }

    @Test
    fun `revertAddition should delete artist for artist addition`() {
        val artist=Artist().apply {id=4}
        val item=contribution("artist",false).apply {changedRecordId=4}
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(item))
        `when`(artistRepository.findArtistById(4)).thenReturn(artist)
        service.revertAddition(1,"trusted")
        verify(artistRepository).delete(artist)
    }

    @Test
    fun `revertAddition should delete band for band addition`() {
        val band=Band().apply {id=4}
        val item=contribution("band",false).apply {changedRecordId=4}
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(item))
        `when`(bandRepository.findBandById(4)).thenReturn(band)
        service.revertAddition(1,"trusted")
        verify(bandRepository).delete(band)
    }

    @Test
    fun `revertAddition should delete membership for band member addition`() {
        val member=BandsMembers().apply {id=4}
        val item=contribution("bands_members",false).apply {changedRecordId=4}
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(item))
        `when`(bandsMemberRepository.findBandsMembersById(4)).thenReturn(member)
        service.revertAddition(1,"trusted")
        verify(bandsMemberRepository).delete(member)
    }

    @Test
    fun `revertAddition should throw RuntimeException for missing album id`() {
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("album",false)))
        assertThrows<RuntimeException> {service.revertAddition(1,"trusted")}
    }

    @Test
    fun `revertAddition should throw RuntimeException for missing artist id`() {
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("artist",false)))
        assertThrows<RuntimeException> {service.revertAddition(1,"trusted")}
    }

    @Test
    fun `revertAddition should throw RuntimeException for missing band id`() {
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("band",false)))
        assertThrows<RuntimeException> {service.revertAddition(1,"trusted")}
    }

    @Test
    fun `revertAddition should throw RuntimeException for missing membership id`() {
        `when`(contributionRepository.getByChangeId(1)).thenReturn(listOf(contribution("bands_members",false)))
        assertThrows<RuntimeException> {service.revertAddition(1,"trusted")}
    }

    private fun contribution(table:String,confirmed:Boolean,action:Action=Action.CREATE)=Contribution().apply {
        changeId=1
        changedTable=table
        this.action=action
        this.confirmed=confirmed
    }
}
