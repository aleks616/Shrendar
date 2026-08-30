package org.aleks616.shrendar.contribution.service

import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionIsAlreadyConfirmedException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ContributionServiceTest {
    private lateinit var contributionRepository:ContributionRepository
    private lateinit var userRepository:UserRepository
    private lateinit var albumService:AlbumService
    private lateinit var artistService:ArtistService
    private lateinit var bandService:BandService
    private lateinit var bandsMemberService:BandsMemberService
    private lateinit var contributionService:ContributionService
    private lateinit var user:User
    private lateinit var contribution:Contribution

    @BeforeEach
    fun setup() {
        contributionRepository=mock(ContributionRepository::class.java)
        userRepository=mock(UserRepository::class.java)
        albumService=mock(AlbumService::class.java)
        artistService=mock(ArtistService::class.java)
        bandService=mock(BandService::class.java)
        bandsMemberService=mock(BandsMemberService::class.java)
        contributionService=ContributionService(
            contributionRepository,
            userRepository,
            albumService,
            artistService,
            bandService,
            bandsMemberService,
        )
        user=User().apply {id=7; login="trusted"; rank=Rank().apply {id=10}}
        contribution=Contribution().apply {
            id=1
            changeId=20
            this.user=this@ContributionServiceTest.user
            action=Action.UPDATE
            changedTable="artist"
            changedColumn="name"
            changedRecordId=3
            oldValue="Old"
            newValue="New"
            changedAt=LocalDateTime.of(2026,1,2,3,4)
            confirmed=false
            confirmedBy=7
        }
    }

    @Test
    fun `getAll should delegate to repository`() {
        `when`(contributionRepository.findAll()).thenReturn(listOf(contribution))
        assertEquals(listOf(contribution),contributionService.getAll())
    }

    @Test
    fun `mapContributionToContributionDto should map every field`() {
        val result=contributionService.mapContributionToContributionDto(listOf(contribution)).single()
        assertEquals(contribution.id,result.id)
        assertEquals(contribution.changeId,result.changeId)
        assertEquals(user.id,result.userId)
        assertEquals(contribution.action,result.action)
        assertEquals(contribution.changedTable,result.changedTable)
        assertEquals(contribution.changedColumn,result.changedColumn)
        assertEquals(contribution.changedRecordId,result.changedRecordId)
        assertEquals(contribution.oldValue,result.oldValue)
        assertEquals(contribution.newValue,result.newValue)
        assertEquals(contribution.changedAt.toString(),result.changedAt)
        assertEquals(contribution.confirmed,result.confirmed)
        assertEquals(contribution.confirmedBy,result.confirmedBy)
    }

    @Test
    fun `getContributionsByRequestingUser should map repository results`() {
        `when`(contributionRepository.findContributionsByUserId(7)).thenReturn(mutableListOf(contribution))
        assertEquals(1,contributionService.getContributionsByRequestingUser(7).size)
    }

    @Test
    fun `getContributionsByConfirmingUser should map repository results`() {
        `when`(contributionRepository.findContributionsByConfirmedBy(7)).thenReturn(mutableListOf(contribution))
        assertEquals(1,contributionService.getContributionsByConfirmingUser(7).size)
    }

    @Test
    fun `getContributionsByTableName should return repository results`() {
        `when`(contributionRepository.findContributionsByChangedTable("artist")).thenReturn(mutableListOf(contribution))
        assertEquals(1,contributionService.getContributionsByTableName("artist").size)
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId should return repository results`() {
        `when`(contributionRepository.findContributionsByChangedTableAndChangedRecordId("artist",3))
            .thenReturn(mutableListOf(contribution))
        assertEquals(1,contributionService.getContributionsByTableNameAndChangedRecordId("artist",3).size)
    }

    @Test
    fun `getLastChangesByTableNameAndChangedRecordId should return table and contributions`() {
        `when`(contributionRepository.findLastContributionsByTableNameAndChangedRecordId("artist",3))
            .thenReturn(mutableListOf(contribution))
        val result=contributionService.getLastChangesByTableNameAndChangedRecordId("artist",3)
        assertEquals("artist",result.table)
        assertEquals(1,result.contributions?.size)
    }

    @Test
    fun `getContributionsByChangedAtBetween should use supplied dates`() {
        val start=LocalDate.of(2026,1,1)
        val end=LocalDate.of(2026,1,3)
        `when`(contributionRepository.findContributionsByChangedAtBetween(start,end)).thenReturn(mutableListOf(contribution))
        val result=contributionService.getContributionsByChangedAtBetween(start,end)
        assertEquals(1,result.size)
        verify(contributionRepository).findContributionsByChangedAtBetween(start,end)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween should use supplied arguments`() {
        val start=LocalDate.of(2026,1,1)
        val end=LocalDate.of(2026,1,3)
        `when`(contributionRepository.findContributionsByChangedAtBetweenAndUser(start,end,7))
            .thenReturn(mutableListOf(contribution))
        val result=contributionService.getContributionsByRequestingUserAndChangedAtBetween(start,end,7)
        assertEquals(1,result.size)
        verify(contributionRepository).findContributionsByChangedAtBetweenAndUser(start,end,7)
    }

    @Test
    fun `getContributionsByActionAndRequestingUser should use action string`() {
        `when`(contributionRepository.findContributionsByActionAndUserId("CREATE",7)).thenReturn(mutableListOf(contribution))
        assertEquals(1,contributionService.getContributionsByActionAndRequestingUser(7,Action.CREATE).size)
    }

    @Test
    fun `confirmDataChangeRequest should throw exception for user below rank 10`() {
        user.rank=Rank().apply {id=9}
        `when`(userRepository.findByLogin("trusted")).thenReturn(user)
        assertThrows<Exception> {contributionService.confirmDataChangeRequest(20,"trusted")}
        verify(contributionRepository,never()).getByChangeId(anyLong())
    }

    @Test
    fun `confirmDataChangeRequest should throw ContributionIsAlreadyConfirmedException for already confirmed contribution`() {
        contribution.confirmed=true
        `when`(userRepository.findByLogin("trusted")).thenReturn(user)
        `when`(contributionRepository.getByChangeId(20)).thenReturn(listOf(contribution))
        `when`(userRepository.findUserById(7)).thenReturn(user)
        assertThrows<ContributionIsAlreadyConfirmedException> {
            contributionService.confirmDataChangeRequest(20,"trusted")
        }
    }

    @Test
    fun `confirmDataChangeRequest should mark all contributions and skip deletion for create`() {
        val second=Contribution().apply {changeId=20; action=Action.CREATE; changedTable="artist"; confirmed=false}
        `when`(userRepository.findByLogin("trusted")).thenReturn(user)
        `when`(contributionRepository.getByChangeId(20)).thenReturn(listOf(contribution,second))
        contributionService.confirmDataChangeRequest(20,"trusted")
        assertEquals(contribution.confirmed,true)
        assertEquals(user.id,contribution.confirmedBy)
        verify(contributionRepository,times(2)).save(any(Contribution::class.java))
        verifyNoInteractions(albumService,artistService,bandService,bandsMemberService)
    }

    @Test
    fun `confirmDataChangeRequest should delete album contribution`() {
        confirmDelete("album")
        verify(albumService).deleteAlbumRequest(3,"trusted",false)
    }

    @Test
    fun `confirmDataChangeRequest should delete artist contribution`() {
        confirmDelete("artist")
        verify(artistService).deleteArtistRequest(3,"trusted",false)
    }

    @Test
    fun `confirmDataChangeRequest should delete band contribution`() {
        confirmDelete("band")
        verify(bandService).deleteBandRequest(3,"trusted",false)
    }

    @Test
    fun `confirmDataChangeRequest should delete band member contribution`() {
        confirmDelete("bands_members")
        verify(bandsMemberService).deleteBandMember(3,"trusted",false)
    }

    private fun confirmDelete(table:String) {
        contribution.action=Action.DELETE
        contribution.changedTable=table
        `when`(userRepository.findByLogin("trusted")).thenReturn(user)
        `when`(contributionRepository.getByChangeId(20)).thenReturn(listOf(contribution))
        contributionService.confirmDataChangeRequest(20,"trusted")
    }
}
