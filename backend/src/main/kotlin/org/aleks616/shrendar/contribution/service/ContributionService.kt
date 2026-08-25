package org.aleks616.shrendar.contribution.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.model.ContributionDto
import org.aleks616.shrendar.contribution.model.ContributionHistoryDto
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionIsAlreadyConfirmedException
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ContributionService(
    private val contributionRepository:ContributionRepository,
    private val userService:UserService,
    private val albumService:AlbumService,
    private val artistService:ArtistService,
    private val bandService:BandService,
    private val bandsMemberService:BandsMemberService

){
    fun getAll():List<Contribution> =contributionRepository.findAll()

    /*fun getContributionCountByUser(userId:Int):Int{
        return contributionRepository.getContributionCountByUser(userId)
    }*/

    @Transactional
    fun confirmDataChangeRequest(changeId:Int,confirmedUserLogin:String){
        val confirmingUser:User=userService.getUserByLogin(confirmedUserLogin)!!
        if(confirmingUser.rank!!.id!!<10) throw Exception("User's rank is too low to confirm contribution request")
        val contributions=contributionRepository.getByChangeId(changeId)
        if(contributions.any{it.confirmed==true}){
            val previousConfirmingUser=userService.getUserById(contributions.first().confirmedBy!!)
            throw ContributionIsAlreadyConfirmedException("Contribution with id $changeId has already been confirmed by user $previousConfirmingUser")
        }
        contributions.forEach {
            it.confirmed=true
            it.confirmedBy=confirmingUser.id
            contributionRepository.save(it)
        }

        if(contributions.first().action==Action.delete){
            if(contributions.first().changedTable=="album")
                albumService.deleteAlbumRequest(contributions.first().changedRecordId!!,confirmedUserLogin,false)
            if(contributions.first().changedTable=="artist")
                artistService.deleteArtistRequest(contributions.first().changedRecordId!!,confirmedUserLogin,false)
            if(contributions.first().changedTable=="band")
                bandService.deleteBandRequest(contributions.first().changedRecordId!!,confirmedUserLogin,false)
            if(contributions.first().changedTable=="bands_members")
                bandsMemberService.deleteBandMemberRequest(contributions.first().changedRecordId!!,confirmedUserLogin,false)
        }
    }

    fun mapContributionToContributionDto(contributions:List<Contribution>):List<ContributionDto>{
        return contributions.map{
            ContributionDto(
                id=it.id,
                changeId=it.changeId,
                userId=it.user?.id,
                action=it.action,
                changedTable=it.changedTable,
                changedColumn=it.changedColumn,
                changedRecordId=it.changedRecordId,
                oldValue=it.oldValue,
                newValue=it.newValue,
                changedAt=it.changedAt.toString(),
                confirmed=it.confirmed,
                confirmedBy=it.confirmedBy
            )
        }
    }
    fun getContributionsByRequestingUser(userId:Int):List<ContributionDto>{
        return mapContributionToContributionDto(contributionRepository.findContributionsByUserId(userId))
    }
    fun getContributionsByConfirmingUser(userId:Int):List<ContributionDto>{
        return mapContributionToContributionDto(contributionRepository.findContributionsByConfirmedBy(userId))
    }

    fun getContributionsByTableName(tableName:String):List<ContributionDto>{
        return mapContributionToContributionDto(contributionRepository.findContributionsByChangedTable(tableName))
    }

    fun getContributionsByTableNameAndChangedRecordId(tableName:String,recordId:Int):List<ContributionDto>{
        return mapContributionToContributionDto(contributionRepository.findContributionsByChangedTableAndChangedRecordId(tableName,recordId))
    }

    fun getLastChangesByTableNameAndChangedRecordId(tableName:String,recordId:Int):ContributionHistoryDto{
        return ContributionHistoryDto(
            table=tableName,
            contributions=mapContributionToContributionDto(contributionRepository.findLastContributionsByTableNameAndChangedRecordId(tableName,recordId))
        )
    }

    fun getContributionsByChangedAtBetween(start:LocalDate,end:LocalDate=LocalDate.now()):List<ContributionDto>{
        return mapContributionToContributionDto(contributionRepository.findContributionsByChangedAtBetween(start,end))
    }

    fun getContributionsByRequestingUserAndChangedAtBetween(start:LocalDate,end:LocalDate=LocalDate.now(),id:Int):List<ContributionDto>{
        return mapContributionToContributionDto( contributionRepository.findContributionsByChangedAtBetweenAndUser(start,end,id))
    }

    fun getContributionsByActionAndRequestingUser(userId:Int,action:Action):List<ContributionDto> {
        return mapContributionToContributionDto(contributionRepository.findContributionsByActionAndUserId(action.toString(),userId))
    }
}