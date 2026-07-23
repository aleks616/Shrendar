package org.aleks616.shrendar.artist.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.model.ChineseZodiacSign
import org.aleks616.shrendar.artist.model.ZodiacSign
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ArtistService(
    private val artistRepository:ArtistRepository,
    private val countryRepository:CountryRepository,
    private val userService:UserService,
    private val rankRepository:RankRepository,
    private val contributionService:ContributionService,
    private val contributionRepository:ContributionRepository
){

    //region query
    fun getAll():List<Artist> {
        return artistRepository.findAll()
    }

    fun getById(id:Int):Artist {
        if(!artistRepository.existsArtistById(id)) throw IllegalArgumentException("artist with id doesn't exist")
        return artistRepository.findArtistById(id)
    }

    fun getByIdWiki(id:Int):ArtistWikiDto {
        val dataRaw=getById(id)
        val daysTillBirthday=Utils.getDaysTillNextAnniversary(dataRaw.birthDate!!)

        var daysTillDeathAnn:Int?=null
        var age:Int
        if(dataRaw.deathDate!=null){
            daysTillDeathAnn=Utils.getDaysTillNextAnniversary(dataRaw.deathDate!!)
            age=dataRaw.birthDate!!.until(dataRaw.deathDate).years
        }
        else{
            age=dataRaw.birthDate!!.until(LocalDate.now()).years
        }
        val gender=if(dataRaw.gender=='M') "Male" else "Female"
        val country=countryRepository.getCountryNameById(dataRaw.country)
        val zodiacSign=getZodiacSign(dataRaw.birthDate!!.monthValue,dataRaw.birthDate!!.dayOfMonth)
        val chineseZodiacSign=getChineseZodiacSign(dataRaw.birthDate!!.year)

        return ArtistWikiDto(
            id=dataRaw.id,
            name=dataRaw.name,
            birthDate=dataRaw.birthDate,
            daysTillBirthday=daysTillBirthday,
            deathDate=dataRaw.deathDate,
            daysTillDeathAnniversary=daysTillDeathAnn,
            age=age,
            gender=gender,
            country=country,
            zodiacSign=zodiacSign,
            chineseZodiacSign=chineseZodiacSign,
            description=dataRaw.description,
            artistImageUrl=dataRaw.artistImageUrl
        )
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

    fun getByBirthday(month:Int,day:Int):List<Artist> {
        return artistRepository.findArtistByBirthDate(month,day)
    }

    fun getByDeathDate(month:Int,day:Int):List<Artist> {
        return artistRepository.findArtistByDeathDate(month,day)
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
    //endregion

    fun getZodiacSign(month:Int,day:Int):ZodiacSign {
        if((month==12&&day>=22&&day<=31)||(month==1&&day>=1&&day<=19))
            return ZodiacSign.CAPRICORN
        else if((month==1&&day>=20&&day<=31)||(month==2&&day>=1&&day<=17))
            return ZodiacSign.AQUARIUS
        else if((month==2&&day>=18&&day<=29)||(month==3&&day>=1&&day<=19))
            return ZodiacSign.PISCES
        else if((month==3&&day>=20&&day<=31)||(month==4&&day>=1&&day<=19))
            return ZodiacSign.ARIES
        else if((month==4&&day>=20&&day<=30)||(month==5&&day>=1&&day<=20))
            return ZodiacSign.TAURUS
        else if((month==5&&day>=21&&day<=31)||(month==6&&day>=1&&day<=20))
            return ZodiacSign.GEMINI
        else if((month==6&&day>=21&&day<=30)||(month==7&&day>=1&&day<=22))
            return ZodiacSign.CANCER
        else if((month==7&&day>=23&&day<=31)||(month==8&&day>=1&&day<=22))
            return ZodiacSign.LEO
        else if((month==8&&day>=23&&day<=31)||(month==9&&day>=1&&day<=22))
            return ZodiacSign.VIRGO
        else if((month==9&&day>=23&&day<=30)||(month==10&&day>=1&&day<=22))
            return ZodiacSign.LIBRA
        else if((month==10&&day>=23&&day<=31)||(month==11&&day>=1&&day<=21))
            return ZodiacSign.SCORPIO
        else if((month==11&&day>=22&&day<=30)||(month==12&&day>=1&&day<=21))
            return ZodiacSign.SAGITTARIUS
        else
            throw IllegalArgumentException("Illegal date")
    }

    fun getChineseZodiacSign(birthYear:Int):ChineseZodiacSign{
        val year=birthYear%12
        return when(year) {
            0->ChineseZodiacSign.MONKEY
            1->ChineseZodiacSign.ROOSTER
            2->ChineseZodiacSign.DOG
            3->ChineseZodiacSign.PIG
            4->ChineseZodiacSign.RAT
            5->ChineseZodiacSign.OX
            6->ChineseZodiacSign.TIGER
            7->ChineseZodiacSign.RABBIT
            8->ChineseZodiacSign.DRAGON
            9->ChineseZodiacSign.SNAKE
            10->ChineseZodiacSign.HORSE
            11->ChineseZodiacSign.GOAT
            else->throw IllegalArgumentException("Illegal year")
        }

    }

    @Transactional
    fun addArtistRequest(artistAddDto:ArtistAddDto,userLogin:String):Boolean{
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val rankLimit=rankRepository.getRankById(requestingUser.rank!!.id!!).allowedContributions!!
        val recentContributionCount=contributionService.getContributionCountByUser(requestingUser.id!!)
        if(recentContributionCount>=rankLimit) return false

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
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

        val lastChangeId=contributionRepository.findTopChangeId()

        val changes:List<Pair<String,String?>> =listOf(
            Pair("artistId",artistId.toString()),
            Pair("name",artistAddDto.name),
            Pair("birth_date",artistAddDto.birthDate.toString()),
            Pair("death_date",artistAddDto.deathDate.toString()),
            Pair("gender",artistAddDto.gender.toString()),
            Pair("country",artistAddDto.country.toString()),
            Pair("description",artistAddDto.description.toString()),
            Pair("artist_image_url",artistAddDto.artistImageUrl.toString()),
        )

        changes.forEach {
            if(it.second!=null){
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.create
                    changedTable="artist"
                    changedColumn=it.first
                    changedRecordId=null
                    oldValue=null
                    newValue=it.second
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }

        return true
    }

    @Transactional
    fun revertArtistAddition(changeId:Int,confirmedUserLogin:String):Boolean {
        val confirmingUser:User=userService.getUserByLogin(confirmedUserLogin)!!
        val rank=confirmingUser.rank!!.id!!
        if(rank<10) return false
        val contributions=contributionRepository.getByChangeId(changeId)
        if(contributions.find { it.confirmed==true }!=null && rank<12) return false

        val artistId=contributions.find {it.changedColumn=="artistId"}?.newValue?.toInt()
        val name=contributions.find {it.changedColumn=="name"}?.newValue

        if(artistId!=null&&name!=null) {
            val artist:Artist=artistRepository.findArtistById(artistId)
            artistRepository.delete(artist)
        }
        else return false

        return true
    }


}