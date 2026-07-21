package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.model.ChineseZodiacSign
import org.aleks616.shrendar.artist.model.ZodiacSign
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class ArtistService(private val artistRepository:ArtistRepository, private val countryRepository:CountryRepository) {

    fun getAll():List<Artist> {
        return artistRepository.findAll()
    }

    fun getById(id:Int):Artist {
        if(!artistRepository.existsArtistById(id)) throw IllegalArgumentException("artist with id doesn't exist")
        return artistRepository.findArtistById(id)
    }

    fun getByIdWiki(id:Int):ArtistWikiDto {
        val dataRaw=getById(id)
        val thisYearBirthDay=LocalDate.of(LocalDate.now().year,dataRaw.birthDate!!.monthValue,dataRaw.birthDate!!.dayOfMonth)
        val nextYearBirthday=LocalDate.of(LocalDate.now().year+1,dataRaw.birthDate!!.monthValue,dataRaw.birthDate!!.dayOfMonth)
        val nextBirthdays=if(thisYearBirthDay.isAfter(LocalDate.now())) thisYearBirthDay else nextYearBirthday
        val daysTillBirthday=LocalDate.now().until(nextBirthdays,ChronoUnit.DAYS)


        var daysTillDeathAnn:Int?=null
        var age:Int
        if(dataRaw.deathDate!=null){
            val thisYearDeathAnn=LocalDate.of(LocalDate.now().year,dataRaw.deathDate!!.monthValue,dataRaw.deathDate!!.dayOfMonth)
            val nextYearDeathAnn=LocalDate.of(LocalDate.now().year+1,dataRaw.deathDate!!.monthValue,dataRaw.deathDate!!.dayOfMonth)
            val nextDeathAnn=if(thisYearDeathAnn.isAfter(LocalDate.now())) thisYearDeathAnn else nextYearDeathAnn
            daysTillDeathAnn=LocalDate.now().until(nextDeathAnn,ChronoUnit.DAYS).toInt()
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
            daysTillBirthday=daysTillBirthday.toInt(),
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

}