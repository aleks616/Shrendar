package org.aleks616.shrendar.artist.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.artist.model.*
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersArtists
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ArtistService(
    private val artistRepository:ArtistRepository,
    private val countryRepository:CountryRepository,
    private val userAccountService:UserAccountService,
    private val contributionRepository:ContributionRepository,
    private val rankService:RankService,
    private val userArtistRepository:UserArtistRepository,
    private val bandsMemberRepository:BandsMemberRepository,
){

    //region query
    fun getAll():List<Artist> {
        return artistRepository.findAll()
    }

    fun getById(id:Long):Artist {
        if(!artistRepository.existsArtistById(id)) throw IllegalArgumentException("artist with id doesn't exist")
        return artistRepository.findArtistById(id)
    }

    fun getByNameLike(name:String):List<Artist> {
        return artistRepository.findArtistByNameContains(name)
    }

    fun getByFirstName(name:String):List<Artist> {
        return artistRepository.findArtistByNameStartsWith(name)
    }

    fun getByLastName(name:String):List<Artist> {
        return artistRepository.findArtistByNameEndsWithIgnoreCase(name)
    }

    fun getByBirthday(month:Int,day:Int):List<ArtistAnniversaryDto> {
        val data=artistRepository.findArtistByBirthDate(month,day)
        val result:MutableList<ArtistAnniversaryDto> = mutableListOf()
        data.forEach {
            val age=it.birthDate!!.until(LocalDate.now()).years
            result.add(ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=0,
                yearsSince=age,
                country=countryRepository.getCountryNameById(it.country),
            ))
        }
        return result
    }

    fun getByDeathDate(month:Int,day:Int):List<ArtistAnniversaryDto> {
        val data=artistRepository.findArtistByDeathDate(month,day)
        val result:MutableList<ArtistAnniversaryDto> = mutableListOf()
        data.forEach {
            val yearsSince=it.deathDate!!.until(LocalDate.now()).years
            result.add(ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.deathDate,
                daysTillAnniversary=0,
                yearsSince=yearsSince,
                country=countryRepository.getCountryNameById(it.country),
            ))
        }
        return result
    }

    fun getByBirthdayBetween(startMonth:Int,startDay:Int,endMonth:Int,endDay:Int):List<Artist> {
        return artistRepository.findArtistByBirthdayBetween(startMonth,startDay,endMonth,endDay)
    }

    fun getByBirthYear(year:Int):List<Artist> {
        return artistRepository.findArtistsByBirthYear(year)
    }

    fun getByBirthYearBetween(startYear:Int,endYear:Int):List<Artist> {
        return artistRepository.findArtistsByBirthYearBetween(startYear,endYear)
    }

    fun getByCountry(countryId:Int):List<Artist> {
        return artistRepository.findArtistByCountry(countryId)
    }

    fun getRecentDeathsAnniversaries():List<Artist> {
        val today=LocalDate.now()
        val recentDate=LocalDate.now().minusDays(30)
        return artistRepository.findArtistByDeathDateBetween(recentDate.monthValue,recentDate.dayOfMonth,today.monthValue,today.dayOfMonth)
    }

    fun getRecentBirthdays():List<Artist> {
        val today=LocalDate.now()
        val recentDate=LocalDate.now().minusDays(30)
        return artistRepository.findArtistByBirthdayBetween(recentDate.monthValue,recentDate.dayOfMonth,today.monthValue,today.dayOfMonth)
    }

    fun getArtistGenres(artistId:Long):ArtistGenreDto {
        return ArtistGenreDto(
            artistId,
            artistRepository.findArtistById(artistId).name,
            artistRepository.findArtistGenres(artistId)
        )
    }
    //endregion

    fun doesArtistExist(artistId:Long):Boolean{
        return artistRepository.existsById(artistId)
    }

    @Transactional
    fun addArtistRequest(artistAddDto:ArtistAddDto,userLogin:String){
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank.id>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        artistRepository.save(Artist().apply {
            name=artistAddDto.name
            birthDate=artistAddDto.birthDate
            deathDate=artistAddDto.deathDate
            gender=artistAddDto.gender
            country=artistAddDto.country
            description=artistAddDto.description
            artistImageUrl=artistAddDto.artistImageUrl
        })

        val artistId=artistRepository.findTopIdByName(artistAddDto.name!!)

        val lastChangeId=contributionRepository.findTopChangeId()?:0

        val changes:List<Pair<String,String>> =listOf(
            Pair("name",artistAddDto.name),
            Pair("birth_date",artistAddDto.birthDate.toString()),
            Pair("death_date",artistAddDto.deathDate.toString()),
            Pair("gender",artistAddDto.gender.toString()),
            Pair("country",artistAddDto.country.toString()),
            Pair("description",artistAddDto.description.toString()),
            Pair("artist_image_url",artistAddDto.artistImageUrl.toString()),
        )

        changes.forEach {
            contributionRepository.save(Contribution().apply {
                changedRecordId=artistId
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.CREATE
                changedTable="artist"
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
    fun editArtistRequest(artistAddDto:ArtistAddDto,userLogin:String){
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val artist=getById(artistAddDto.id!!)
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

        updateIfChanged("name",artist.name,artistAddDto.name,{artist.name=it})
        updateIfChanged("birth_date",artist.birthDate,artistAddDto.birthDate,{artist.birthDate=it})
        updateIfChanged("death_date",artist.deathDate,artistAddDto.deathDate,{artist.deathDate=it})
        updateIfChanged("gender",artist.gender,artistAddDto.gender,{artist.gender=it})
        updateIfChanged("country",artist.country,artistAddDto.country,{artist.country=it})
        updateIfChanged("description",artist.description,artistAddDto.description,{artist.description=it})
        updateIfChanged("artist_image_url",artist.artistImageUrl,artistAddDto.artistImageUrl,{artist.artistImageUrl=it})

        if(changes.isEmpty()) throw IllegalStateException("no changes found")

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank.id>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        artistRepository.save(artist)
        val lastChangeId=contributionRepository.findTopChangeId()?:0
        changes.forEach { (column,oldValue,newValue)->
            contributionRepository.save(Contribution().apply {
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.UPDATE
                changedTable="artist"
                changedColumn=column
                changedRecordId=artistAddDto.id
                this.oldValue=oldValue
                this.newValue=newValue
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }

    }

    @Transactional
    fun deleteArtistRequest(artistId:Long,userLogin:String,log:Boolean=true) {
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank.id>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        if(log){
            val artist=getById(artistId)
            val changes:List<Triple<String,String?,String?>> =listOf(
                Triple("id",artist.id.toString(),null),
                Triple("name",artist.name,null),
                Triple("birth_date",artist.birthDate.toString(),null),
                Triple("death_date",artist.deathDate.toString(),null),
                Triple("gender",artist.gender.toString(),null),
                Triple("country",artist.country.toString(),null),
                Triple("description",artist.description.toString(),null),
                Triple("artist_image_url",artist.artistImageUrl.toString(),null),
            )

            val lastChangeId=contributionRepository.findTopChangeId()?:0
            changes.forEach {(column,oldValue,newValue)->
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.DELETE
                    changedTable="artist"
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
            artistRepository.deleteById(artistId)
        }

    }

    @Transactional
    fun toggleFavoriteArtist(artistId:Long,login:String){
        val user=userAccountService.getUserByLogin(login)?:throw IllegalStateException("User not found")
        val artist=artistRepository.findArtistById(artistId)
        val recordId=userArtistRepository.findByArtistAndUser(artist,user)?.id
        if(recordId==null){
            userArtistRepository.saveAndFlush(UsersArtists().apply {
                this.user=user
                this.artist=artist
            })
        }
        else
            userArtistRepository.deleteById(recordId)
    }

    @Transactional
    fun toggleFavoriteArtistByBand(bandId:Int,login:String){
        val user=userAccountService.getUserByLogin(login)?:throw IllegalStateException("User not found")
        val bandMembers=bandsMemberRepository.findByBandId(bandId)
        val artists=bandMembers.map {it.artist!!}
        artists.forEach { a->
            val recordId=userArtistRepository.findByArtistAndUser(a,user)?.id
            if(recordId==null) toggleFavoriteArtist(a.id!!,login)
        }
    }
}