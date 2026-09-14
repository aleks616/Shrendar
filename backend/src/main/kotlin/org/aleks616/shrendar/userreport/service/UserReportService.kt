package org.aleks616.shrendar.userreport.service

import org.aleks616.shrendar.userreport.model.ReportDetailsDto
import org.aleks616.shrendar.userreport.model.ReportRequestDto
import org.aleks616.shrendar.userreport.model.ReportsByUserDto
import org.aleks616.shrendar.userreport.model.UsersReport
import org.aleks616.shrendar.userreport.model.UsersReportDto
import org.aleks616.shrendar.user.repository.UserRepository
import org.aleks616.shrendar.user.service.UserAccountService
import org.aleks616.shrendar.userreport.repository.UsersReportRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserReportService(
    private val userRepository:UserRepository,
    private val userAccountService:UserAccountService,
    private val usersReportRepository:UsersReportRepository,
) {
    fun reportUser(report:ReportRequestDto,requesting:String){
        if(report.reportedUserId==0||!userAccountService.doesUserExist(report.reportedUserId)) throw Exception("User not found")
        usersReportRepository.save(
            UsersReport().apply {
                reportedUser=userRepository.findUserById(report.reportedUserId)?:throw Exception("User not found")
                requestingUser=userRepository.findByLogin(requesting)?:throw Exception("User not found")
                at=Instant.now()
                description=report.reason
            }
        )
    }
    fun getUserReportsByUserId(userId:Int):ReportsByUserDto?{
        val user=userRepository.findUserById(userId)?:throw Exception("User not found")

        return usersReportRepository.findByReportedUser(user).toReportsByUserDto()
    }
    fun getNotResolvedReports():List<UsersReportDto>{
        return usersReportRepository.findByResolved(false).map {
            UsersReportDto(
                id=it.id,
                reportedUserId=it.reportedUser.id,
                reportedUserLogin=it.reportedUser.login,
                reportedUserUsername=it.reportedUser.username,
                reportedUserRankId=it.reportedUser.rank.id,
                reportedUserBirthDate=it.reportedUser.birthDate,
                reportedUserXp=it.reportedUser.xp,
                reportedUserVerified=it.reportedUser.verified,
                reportedUserBio=it.reportedUser.bio,
                requestingUserId=it.requestingUser.id,
                requestingUserLogin=it.requestingUser.login,
                requestingUserRankId=it.requestingUser.rank.id,
                at=it.at,
                resolved=it.resolved,
                description=it.description,
            )
        }
    }
    fun canReport(reportUserId:Int,requestingUserLogin:String):Boolean{
        val requestingUser=userRepository.findByLogin(requestingUserLogin)?.id?:return false
        val since=Instant.now().minusSeconds(14*24*60*60)
        val data=usersReportRepository.findRecentByUser(requestingUser,reportUserId,since)
        return !data.any()
    }
    fun resolveReport(id:Long){
        val report=usersReportRepository.findUsersReportById(id)
        if(report.resolved) throw Exception("Report already resolved")

        report.resolved=true
        usersReportRepository.save(report)
    }

    fun List<UsersReport>.toReportsByUserDto():ReportsByUserDto?{
        val user=firstOrNull()?.reportedUser?:return null
        return ReportsByUserDto(
            userId=user.id,
            login=user.login,
            username=user.username,
            rankId=user.rank.id,
            birthDate=user.birthDate,
            xp=user.xp,
            verified=user.verified,
            bio=user.bio,
            reports=mapNotNull {r->
                r.requestingUser.let {
                    ReportDetailsDto(
                        id=r.id,
                        requestingUserId=it.id,
                        requestingUserLogin=it.login,
                        requestingUserRankId=it.rank.id,
                        at=r.at,
                        resolved=r.resolved,
                        description=r.description
                    )
                }
            }
        )
    }

}