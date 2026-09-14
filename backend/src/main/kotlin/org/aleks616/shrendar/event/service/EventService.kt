package org.aleks616.shrendar.event.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.event.model.Event
import org.aleks616.shrendar.event.model.EventAddDto
import org.aleks616.shrendar.event.repository.EventRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventService(
    private val eventRepository:EventRepository,
    private val bandService:BandService,
    private val contributionRepository:ContributionRepository,
    private val userAccountService:UserAccountService,
    private val rankService:RankService,
){
    fun doesEventExist(eventId:Int):Boolean {
        return eventRepository.existsById(eventId)
    }

    @Transactional
    fun addEventRequest(eventAddDto:EventAddDto,userLogin:String) {
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        val savedEvent=eventRepository.save(Event().apply {
            band=bandService.getBandById(eventAddDto.bandId!!)
            date=eventAddDto.date
            name=eventAddDto.name
            description=eventAddDto.description
        })

        val eventId=savedEvent.id!!.toLong()
        val lastChangeId=contributionRepository.findTopChangeId()?:0

        val changes:List<Pair<String,String>> =listOf(
            Pair("band_id",eventAddDto.bandId.toString()),
            Pair("date",eventAddDto.date.toString()),
            Pair("name",eventAddDto.name.toString()),
            Pair("description",eventAddDto.description.toString()),
        )

        changes.forEach {
            contributionRepository.save(Contribution().apply {
                changedRecordId=eventId
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.CREATE
                changedTable="event"
                changedColumn=it.first
                oldValue=null
                newValue=it.second
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }
    }

    @Transactional
    fun editEventRequest(eventAddDto:EventAddDto,userLogin:String) {
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val event=eventRepository.findEventById(eventAddDto.id!!)
        val changes=mutableListOf<Triple<String,String?,String?>>()

        fun <T> updateIfChanged(
            column:String,
            currentValue:T?,
            newValue:T?,
            setter:(T)->Unit,
            stringMapper:(T?)->String?={it?.toString()}
        ) {
            if(newValue!=null&&newValue!=currentValue) {
                changes.add(Triple(column,stringMapper(currentValue),stringMapper(newValue)))
                setter(newValue)
            }
        }

        updateIfChanged("band_id",event.band?.id,eventAddDto.bandId,{event.band=bandService.getBandById(it)})
        updateIfChanged("date",event.date,eventAddDto.date,{event.date=it})
        updateIfChanged("name",event.name,eventAddDto.name,{event.name=it})
        updateIfChanged("description",event.description,eventAddDto.description,{event.description=it})

        if(changes.isEmpty()) throw IllegalStateException("no changes found")

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        eventRepository.save(event)
        val lastChangeId=contributionRepository.findTopChangeId()?:0
        changes.forEach {(column,oldValue,newValue)->
            contributionRepository.save(Contribution().apply {
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.UPDATE
                changedTable="event"
                changedColumn=column
                changedRecordId=eventAddDto.id.toLong()
                this.oldValue=oldValue
                this.newValue=newValue
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }
    }

    @Transactional
    fun deleteEventRequest(eventId:Int,userLogin:String,log:Boolean=true) {
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        if(log){
            val event=eventRepository.findEventById(eventId)
            val changes:List<Triple<String,String?,String?>> =listOf(
                Triple("id",event.id.toString(),null),
                Triple("band_id",event.band?.id.toString(),null),
                Triple("date",event.date.toString(),null),
                Triple("name",event.name,null),
                Triple("description",event.description,null),
            )

            val lastChangeId=contributionRepository.findTopChangeId()?:0
            changes.forEach {(column,oldValue,newValue)->
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.DELETE
                    changedTable="event"
                    changedColumn=column
                    changedRecordId=eventId.toLong()
                    this.oldValue=oldValue
                    this.newValue=newValue
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }

        if(trusted){
            eventRepository.deleteById(eventId)
        }
    }

}
