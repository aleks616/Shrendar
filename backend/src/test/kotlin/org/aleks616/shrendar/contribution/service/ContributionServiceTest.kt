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
import org.aleks616.shrendar.user.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ContributionServiceTest {
    private lateinit var repository:ContributionRepository
    private lateinit var userService:UserService
    private lateinit var albumService:AlbumService
    private lateinit var artistService:ArtistService
    private lateinit var bandService:BandService
    private lateinit var bandsMemberService:BandsMemberService
    private lateinit var service:ContributionService
    private lateinit var user:User
    private lateinit var contribution:Contribution

    @BeforeEach
    fun setup() {
        repository=mock(ContributionRepository::class.java)
        userService=mock(UserService::class.java)
        albumService=mock(AlbumService::class.java)
        artistService=mock(ArtistService::class.java)
        bandService=mock(BandService::class.java)
        bandsMemberService=mock(BandsMemberService::class.java)
        service=ContributionService(repository,userService,albumService,artistService,bandService,bandsMemberService)
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
    fun `getAll delegates to repository`() {
        `when`(repository.findAll()).thenReturn(listOf(contribution))
        assertEquals(listOf(contribution),service.getAll())
    }

    @Test
    fun `mapContributionToContributionDto maps every field`() {
        val result=service.mapContributionToContributionDto(listOf(contribution)).single()
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
    fun `getContributionsByRequestingUser maps repository results`() {
        `when`(repository.findContributionsByUserId(7)).thenReturn(mutableListOf(contribution))
        assertEquals(1,service.getContributionsByRequestingUser(7).size)
    }

    @Test
    fun `getContributionsByConfirmingUser maps repository results`() {
        `when`(repository.findContributionsByConfirmedBy(7)).thenReturn(mutableListOf(contribution))
        assertEquals(1,service.getContributionsByConfirmingUser(7).size)
    }

    @Test
    fun `getContributionsByTableName maps repository results`() {
        `when`(repository.findContributionsByChangedTable("artist")).thenReturn(mutableListOf(contribution))
        assertEquals(1,service.getContributionsByTableName("artist").size)
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId maps repository results`() {
        `when`(repository.findContributionsByChangedTableAndChangedRecordId("artist",3))
            .thenReturn(mutableListOf(contribution))
        assertEquals(1,service.getContributionsByTableNameAndChangedRecordId("artist",3).size)
    }

    @Test
    fun `getLastChangesByTableNameAndChangedRecordId returns table and contributions`() {
        `when`(repository.findLastContributionsByTableNameAndChangedRecordId("artist",3))
            .thenReturn(mutableListOf(contribution))
        val result=service.getLastChangesByTableNameAndChangedRecordId("artist",3)
        assertEquals("artist",result.table)
        assertEquals(1,result.contributions?.size)
    }

    @Test
    fun `getContributionsByChangedAtBetween uses supplied dates`() {
        val start=LocalDate.of(2026,1,1)
        val end=LocalDate.of(2026,1,3)
        `when`(repository.findContributionsByChangedAtBetween(start,end)).thenReturn(mutableListOf(contribution))
        val result=service.getContributionsByChangedAtBetween(start,end)
        assertEquals(1,result.size)
        verify(repository).findContributionsByChangedAtBetween(start,end)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween uses supplied arguments`() {
        val start=LocalDate.of(2026,1,1)
        val end=LocalDate.of(2026,1,3)
        `when`(repository.findContributionsByChangedAtBetweenAndUser(start,end,7))
            .thenReturn(mutableListOf(contribution))
        val result=service.getContributionsByRequestingUserAndChangedAtBetween(start,end,7)
        assertEquals(1,result.size)
        verify(repository).findContributionsByChangedAtBetweenAndUser(start,end,7)
    }

    @Test
    fun `getContributionsByActionAndRequestingUser uses action string`() {
        `when`(repository.findContributionsByActionAndUserId("CREATE",7)).thenReturn(mutableListOf(contribution))
        assertEquals(1,service.getContributionsByActionAndRequestingUser(7,Action.CREATE).size)
    }

    @Test
    fun `confirm rejects a user below rank 10`() {
        user.rank=Rank().apply {id=9}
        `when`(userService.getUserByLogin("trusted")).thenReturn(user)
        assertThrows<Exception> {service.confirmDataChangeRequest(20,"trusted")}
        verify(repository,never()).getByChangeId(anyLong())
    }

    @Test
    fun `confirm rejects an already confirmed contribution`() {
        contribution.confirmed=true
        `when`(userService.getUserByLogin("trusted")).thenReturn(user)
        `when`(repository.getByChangeId(20)).thenReturn(listOf(contribution))
        `when`(userService.getUserById(7)).thenReturn(user)
        assertThrows<ContributionIsAlreadyConfirmedException> {
            service.confirmDataChangeRequest(20,"trusted")
        }
    }

    @Test
    fun `confirm marks all contributions and skips deletion for create`() {
        val second=Contribution().apply {changeId=20; action=Action.CREATE; changedTable="artist"; confirmed=false}
        `when`(userService.getUserByLogin("trusted")).thenReturn(user)
        `when`(repository.getByChangeId(20)).thenReturn(listOf(contribution,second))
        service.confirmDataChangeRequest(20,"trusted")
        assertEquals(contribution.confirmed,true)
        assertEquals(user.id,contribution.confirmedBy)
        verify(repository,times(2)).save(any(Contribution::class.java))
        verifyNoInteractions(albumService,artistService,bandService,bandsMemberService)
    }

    @Test
    fun `confirm deletes an album contribution`() {
        confirmDelete("album")
        verify(albumService).deleteAlbumRequest(3,"trusted",false)
    }

    @Test
    fun `confirm deletes an artist contribution`() {
        confirmDelete("artist")
        verify(artistService).deleteArtistRequest(3,"trusted",false)
    }

    @Test
    fun `confirm deletes a band contribution`() {
        confirmDelete("band")
        verify(bandService).deleteBandRequest(3,"trusted",false)
    }

    @Test
    fun `confirm deletes a band member contribution`() {
        confirmDelete("bands_members")
        verify(bandsMemberService).deleteBandMemberRequest(3,"trusted",false)
    }

    private fun confirmDelete(table:String) {
        contribution.action=Action.DELETE
        contribution.changedTable=table
        `when`(userService.getUserByLogin("trusted")).thenReturn(user)
        `when`(repository.getByChangeId(20)).thenReturn(listOf(contribution))
        service.confirmDataChangeRequest(20,"trusted")
    }
}
