package org.aleks616.shrendar.userreport.service

import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.UserRepository
import org.aleks616.shrendar.user.service.UserAccountService
import org.aleks616.shrendar.userreport.model.ReportRequestDto
import org.aleks616.shrendar.userreport.model.UsersReport
import org.aleks616.shrendar.userreport.repository.UsersReportRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class UserReportServiceTest {
    private val users=mock(UserRepository::class.java)
    private val accounts=mock(UserAccountService::class.java)
    private val reports=mock(UsersReportRepository::class.java)
    private lateinit var service:UserReportService
    private lateinit var reported:User
    private lateinit var requester:User

    @BeforeEach fun setup() {
        service=UserReportService(users,accounts,reports)
        reported=user(2,"reported",1)
        requester=user(1,"reporter",2)
    }

    @Test fun `reportUser rejects zero and nonexistent targets then saves valid reports`() {
        assertThrows<Exception> {service.reportUser(ReportRequestDto(0,"reason"),"reporter")}
        `when`(accounts.doesUserExist(2)).thenReturn(false)
        assertThrows<Exception> {service.reportUser(ReportRequestDto(2,"reason"),"reporter")}
        `when`(accounts.doesUserExist(2)).thenReturn(true)
        `when`(users.findUserById(2)).thenReturn(reported)
        `when`(users.findByLogin("reporter")).thenReturn(requester)
        service.reportUser(ReportRequestDto(2,"reason"),"reporter")
        val saved=ArgumentCaptor.forClass(UsersReport::class.java)
        verify(reports).save(saved.capture())
        assertSame(reported,saved.value.reportedUser)
        assertSame(requester,saved.value.requestingUser)
        assertEquals("reason",saved.value.description)
        assertNotNull(saved.value.at)
    }

    @Test fun `reportUser throws when reported user cannot be loaded`() {
        `when`(accounts.doesUserExist(2)).thenReturn(true)
        `when`(users.findUserById(2)).thenReturn(null)

        assertThrows<Exception> {service.reportUser(ReportRequestDto(2,"reason"),"reporter")}

        verify(reports,never()).save(any())
    }

    @Test fun `reportUser throws when requesting user cannot be loaded`() {
        `when`(accounts.doesUserExist(2)).thenReturn(true)
        `when`(users.findUserById(2)).thenReturn(reported)
        `when`(users.findByLogin("reporter")).thenReturn(null)

        assertThrows<Exception> {service.reportUser(ReportRequestDto(2,"reason"),"reporter")}

        verify(reports,never()).save(any())
    }

    @Test fun `reports by user dto is null for an empty report list`() {
        assertNull(service.run {emptyList<UsersReport>().toReportsByUserDto()})
    }

    @Test fun `reports by user dto is null when the first report has no reported user`() {
        val reportWithoutReportedUser=mock(UsersReport::class.java)
        doReturn(null).`when`(reportWithoutReportedUser).reportedUser

        assertNull(service.run {listOf(reportWithoutReportedUser).toReportsByUserDto()})
    }

    @Test fun `reports by user reject missing users and map reporters while excluding malformed records`() {
        `when`(users.findUserById(2)).thenReturn(null)
        assertThrows<Exception> {service.getUserReportsByUserId(2)}
        `when`(users.findUserById(2)).thenReturn(reported)
        `when`(reports.findByReportedUser(reported)).thenReturn(mutableListOf(report(4),UsersReport().apply {id=5; reportedUser=reported}))
        val result=service.getUserReportsByUserId(2)!!
        assertEquals("reported",result.login)
        assertEquals(listOf(4L,5L),result.reports!!.map {it.id})
        `when`(reports.findByReportedUser(reported)).thenReturn(mutableListOf())
        assertNull(service.getUserReportsByUserId(2))
    }

    @Test fun `unresolved reports map both user and report fields`() {
        `when`(reports.findByResolved(false)).thenReturn(mutableListOf(report(4).apply {resolved=false}))
        val result=service.getNotResolvedReports().single()
        assertEquals(2,result.reportedUserId)
        assertEquals("reporter",result.requestingUserLogin)
        assertEquals("reason",result.description)
    }

    @Test fun `canReport rejects missing requester and recent reports only`() {
        `when`(users.findByLogin("missing")).thenReturn(null)
        assertFalse(service.canReport(2,"missing"))
        `when`(users.findByLogin("reporter")).thenReturn(requester)
        `when`(reports.findRecentByUser(eq(1),eq(2),anyInstant())).thenReturn(emptyList())
        assertTrue(service.canReport(2,"reporter"))
        `when`(reports.findRecentByUser(eq(1),eq(2),anyInstant())).thenReturn(listOf(report(4)))
        assertFalse(service.canReport(2,"reporter"))
    }

    @Test fun `resolveReport rejects already resolved reports and saves newly resolved reports`() {
        val resolved=report(4).apply {resolved=true}
        `when`(reports.findUsersReportById(4)).thenReturn(resolved)
        assertThrows<Exception> {service.resolveReport(4)}
        resolved.resolved=false
        service.resolveReport(4)
        assertTrue(resolved.resolved)
        verify(reports).save(resolved)
    }

    private fun user(id:Int,login:String,rank:Int)=User().apply {
        this.id=id; this.login=login; username="$login name"; this.rank=Rank().apply {this.id=rank}; xp=20; verified=true
    }
    private fun report(id:Long)=UsersReport().apply {
        this.id=id; reportedUser=this@UserReportServiceTest.reported; requestingUser=this@UserReportServiceTest.requester; description="reason"
    }
    private fun anyInstant()=any(java.time.Instant::class.java)?:java.time.Instant.EPOCH
}
