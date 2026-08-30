package org.aleks616.shrendar.band.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BandsMemberService(
    val artistService:ArtistService,
    val bandService:BandService,
    val bandsMemberRepository:BandsMemberRepository,
    val contributionRepository:ContributionRepository,
    val userAccountService:UserAccountService,
    val rankService:RankService,
) {
    fun doesBandMemberExist(id:Long):Boolean {
        return bandsMemberRepository.existsById(id)
    }
    fun getBandMembersRaw(band:Int):List<BandsMembersDataDto> {
        return bandsMemberRepository.findAllByBandName(band)
    }

    fun getAllBandMembers(band:Int):List<BandsMembersDto> {
        val dataRaw=getBandMembersRaw(band)
        val data:List<BandsMembersDataExtendedDto> =dataRaw.map {d->
            BandsMembersDataExtendedDto(
                id=d.id,
                artistId=d.artistId,
                artistName=d.artistName,
                bandId=d.bandId,
                bandName=d.bandName,
                role=d.role,
                joinedYear=d.joinedYear,
                leftYear=d.leftYear,
                nickname=d.nickname,
                yearRole=mutableListOf(),
            )
        }

        val result:MutableList<BandsMembersDto> =mutableListOf()
        var found:Boolean

        data.forEach {d->
            found=false
            val left:String=if(d.leftYear==null) "" else d.leftYear.toString()
            val yearRole:String=
                if(d.joinedYear!=d.leftYear) ("${d.role} (${d.joinedYear}-${left})")
                else ("${d.role} (${d.joinedYear})")

            result.forEach {r->
                if(r.artistId==d.artistId) {
                    found=true
                    r.yearRole?.add(yearRole)
                }
            }

            if(!found) {
                d.yearRole?.add(yearRole)
                result.add(
                    BandsMembersDto(
                        id=d.id,
                        artistId=d.artistId,
                        artistName=d.artistName,
                        bandId=d.bandId,
                        bandName=d.bandName,
                        yearRole=d.yearRole,
                        nickname=d.nickname
                    )
                )
            }
        }
        return result
    }

    fun getAllBandMembersWiki(id:Int):List<BandsMembersWikiDto>{ //todo type
        val dataRaw=getAllBandMembers(id)
        val data=dataRaw.map {BandsMembersWikiDto(
            id=it.id,
            artistId=it.artistId,
            artistName=it.artistName,
            bandId=it.bandId,
            nickname=it.nickname,
            yearRole=it.yearRole
        )}
        return data
    }

    fun getCurrentBandMembers(band:Int):List<BandsMembersDto> {
       val allData=getAllBandMembers(band)
        return allData.filter {d-> d.yearRole?.any {it.contains("-)")} ?: false}
    }


    fun getPastBandMembers(band:Int):List<BandsMembersDto> {
        val allData=getAllBandMembers(band)
        val currentData=getCurrentBandMembers(band)
        return allData.filter {d-> d.artistId !in currentData.map {it.artistId}}
    }

    fun getBandsByArtistId(id:Long):List<ArtistBandsHistoryDto>{
        val dataRaw=bandsMemberRepository.findBandsByArtistId(id)
        val data:List<ArtistBandsExtendedDto> =dataRaw.map {d->
            ArtistBandsExtendedDto(
                id=d.id,
                artistId=d.artistId,
                artistName=d.artistName,
                bandId=d.bandId,
                bandName=d.bandName,
                role=d.role,
                joinedYear=d.joinedYear,
                leftYear=d.leftYear,
                nickname=d.nickname,
                yearRole=mutableListOf(),
            )
        }

        val result:MutableList<ArtistBandsHistoryDto> =mutableListOf()
        var found:Boolean

        data.forEach { d->
            found=false
            val left:String=if(d.leftYear==null) "" else d.leftYear.toString()
            val yearRole:String=if(d.joinedYear!=d.leftYear) ("${d.role} (${d.joinedYear}-${left})") else d.joinedYear.toString()
            result.forEach {r->
                if(r.bandId==d.bandId) {
                    found=true
                    r.yearRole?.add(yearRole)
                }
            }
            if(!found){
                d.yearRole?.add(yearRole)
                result.add(
                    ArtistBandsHistoryDto(
                        id=d.id,
                        artistId=d.artistId,
                        artistName=d.artistName,
                        bandId=d.bandId,
                        bandName=d.bandName,
                        nickname=d.nickname,
                        yearRole=d.yearRole
                    )
                )
            }
        }


        return result
    }

    fun getArtistBandsList(id:Long):List<ArtistBandsStatusDto>{
        val dataRaw=bandsMemberRepository.findBandsByArtistId(id).distinctBy {it.bandId}
        return dataRaw.map { d->
            ArtistBandsStatusDto(
                artistId=d.artistId,
                artistName=d.artistName,
                bandId=d.bandId,
                bandName=d.bandName,
                current=d.leftYear==null,
            )
        }
    }

    @Transactional
    fun addBandMemberRequest(artistBandAddDto:ArtistBandAddDto,userLogin:String) {
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>8) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        bandsMemberRepository.save(BandsMembers().apply {
            artist=artistService.getById(artistBandAddDto.artistId!!)
            band=bandService.getBandById(artistBandAddDto.bandId!!)
            role=artistBandAddDto.role
            joinedYear=artistBandAddDto.joinedYear
            leftYear=artistBandAddDto.leftYear
            nickname=artistBandAddDto.nickname
        })

        val lastChangeId=contributionRepository.findTopChangeId()?:0

        val changes:List<Pair<String,String>> =listOf(
            Pair("bandId",artistBandAddDto.bandId.toString()),
            Pair("artistId",artistBandAddDto.artistId.toString()),
            Pair("role",artistBandAddDto.role.toString()),
            Pair("joinedYear",artistBandAddDto.joinedYear.toString()),
            Pair("leftYear",artistBandAddDto.leftYear.toString()),
            Pair("nickname",artistBandAddDto.nickname.toString())
        )
        val bandMemberId=bandsMemberRepository.findTopIdByBandIdAndArtistId(artistBandAddDto.bandId!!,artistBandAddDto.artistId!!)

        changes.forEach {
            contributionRepository.save(Contribution().apply {
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.CREATE
                changedTable="bands_members"
                changedColumn=it.first
                changedRecordId=bandMemberId
                oldValue=null
                newValue=it.second
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }
    }

    @Transactional
    fun editBandMemberRequest(artistBandAddDto:ArtistBandAddDto,userLogin:String) {
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val bandMember=bandsMemberRepository.findById(artistBandAddDto.id!!)
        if(artistBandAddDto.leftYear!=null&&bandMember.joinedYear!=null&&bandMember.joinedYear!!>artistBandAddDto.leftYear!!)
            throw IllegalArgumentException("Left year has to be the same or greater than joined year")
        if(artistBandAddDto.joinedYear!=null&&bandMember.leftYear!=null&&artistBandAddDto.joinedYear!!>bandMember.leftYear!!)
            throw IllegalArgumentException("Joined year has to be the same or less than left year")

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

        updateIfChanged("band_id",bandMember.band!!.id,artistBandAddDto.bandId,{bandMember.band=bandService.getBandById(it)})
        updateIfChanged("artist_id",bandMember.artist!!.id,artistBandAddDto.artistId,{bandMember.artist=artistService.getById(it)})
        updateIfChanged("nickname",bandMember.nickname,artistBandAddDto.nickname,{bandMember.nickname=it})
        updateIfChanged("role",bandMember.role,artistBandAddDto.role,{bandMember.role=it})
        updateIfChanged("joined_year",bandMember.joinedYear,artistBandAddDto.joinedYear,{bandMember.joinedYear=it})
        updateIfChanged("left_year",bandMember.leftYear,artistBandAddDto.leftYear,{bandMember.leftYear=it})

        if(changes.isEmpty()) throw IllegalStateException("no changes found")

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        bandsMemberRepository.save(bandMember)
        val lastChangeId=contributionRepository.findTopChangeId()?:0
        changes.forEach { (column,oldValue,newValue)->
            contributionRepository.save(Contribution().apply {
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.UPDATE
                changedTable="bands_members"
                changedColumn=column
                changedRecordId=artistBandAddDto.id
                this.oldValue=oldValue
                this.newValue=newValue
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }
    }

    @Transactional
    fun deleteBandMemberRequest(id:Long,userLogin:String,log:Boolean=true) {
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
            val bandMember=bandsMemberRepository.findById(id)
            val changes:List<Triple<String,String?,String?>> =listOf(
                Triple("id",bandMember.id.toString(),null),
                Triple("band_id",bandMember.band?.id.toString(),null),
                Triple("artist_id",bandMember.artist?.id.toString(),null),
                Triple("nickname",bandMember.nickname,null),
                Triple("role",bandMember.role,null),
                Triple("joined_year",bandMember.joinedYear.toString(),null),
                Triple("left_year",bandMember.leftYear.toString(),null),
            )

            val lastChangeId=contributionRepository.findTopChangeId()?:0
            changes.forEach {(column,oldValue,newValue)->
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.DELETE
                    changedTable="bands_members"
                    changedColumn=column
                    changedRecordId=id
                    this.oldValue=oldValue
                    this.newValue=newValue
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }

        if(trusted){
            bandsMemberRepository.deleteById(id)
        }
    }

}


