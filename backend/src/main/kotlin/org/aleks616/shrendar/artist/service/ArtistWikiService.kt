package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.model.ChineseZodiacSign
import org.aleks616.shrendar.artist.model.ZodiacSign
import org.aleks616.shrendar.band.model.ArtistBandsExtendedDto
import org.aleks616.shrendar.band.model.ArtistBandsHistoryDto
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.collections.forEach

@Service
class ArtistWikiService(
    private val artistService:ArtistService,
    private val countryRepository:CountryRepository,
    private val bandsMemberRepository:BandsMemberRepository,
    private val userArtistRepository:UserArtistRepository
) {

    fun getByIdWiki(id:Long,login:String):ArtistWikiDto {
        val dataRaw=artistService.getById(id)
        val daysTillBirthday=Utils.getDaysTillNextAnniversary(dataRaw.birthDate)

        var daysTillDeathAnn:Int?=null
        var age:Int?
        if(dataRaw.deathDate!=null){
            daysTillDeathAnn=Utils.getDaysTillNextAnniversary(dataRaw.deathDate)
            age=dataRaw.birthDate?.until(dataRaw.deathDate)?.years
        }
        else{
            age=dataRaw.birthDate?.until(LocalDate.now())?.years
        }
        val gender=when(dataRaw.gender){
            'M'->"Male"
            'F'->"Female"
            else->"Unknown"
        }

        val country=countryRepository.getCountryNameById(dataRaw.country)
        val zodiacSign=dataRaw.birthDate?.let { getZodiacSign(it.monthValue,it.dayOfMonth) }
        val chineseZodiacSign=dataRaw.birthDate?.let { getChineseZodiacSign(it.year) }
        val isFavorite:Boolean=userArtistRepository.existsByArtistIdAndUser_Login(id,login)

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
            artistImageUrl=dataRaw.artistImageUrl,
            bands=getBandsByArtistId(id),
            favorite=isFavorite
        )
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
                        memberId=d.id,
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

    fun getZodiacSign(month:Int,day:Int):ZodiacSign {
        if((month==12&&day>=22)||(month==1&&day<=19))
            return ZodiacSign.CAPRICORN
        else if((month==1)||(month==2&&day<=17))
            return ZodiacSign.AQUARIUS
        else if((month==2)||(month==3&&day<=19))
            return ZodiacSign.PISCES
        else if((month==3)||(month==4&&day<=19))
            return ZodiacSign.ARIES
        else if((month==4)||(month==5&&day<=20))
            return ZodiacSign.TAURUS
        else if((month==5)||(month==6&&day<=20))
            return ZodiacSign.GEMINI
        else if((month==6)||(month==7&&day<=22))
            return ZodiacSign.CANCER
        else if((month==7)||(month==8&&day<=22))
            return ZodiacSign.LEO
        else if((month==8)||(month==9&&day<=22))
            return ZodiacSign.VIRGO
        else if((month==9)||(month==10&&day<=22))
            return ZodiacSign.LIBRA
        else if((month==10)||(month==11&&day<=21))
            return ZodiacSign.SCORPIO
        else if((month==11)||(month==12))
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

}
