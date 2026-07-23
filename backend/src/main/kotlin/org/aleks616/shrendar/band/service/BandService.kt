package org.aleks616.shrendar.band.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.genre.service.GenreSimilarity
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BandService(
    private val bandRepository:BandRepository,
    private val countryRepository:CountryRepository,
    private val genreService:GenreService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val genreRepository:GenreRepository,
    private val contributionService:ContributionService,
    private val userService:UserService,
    private val rankRepository:RankRepository,
    private val contributionRepository:ContributionRepository
){
    //region util
    fun getBandsCountry(bandId:Int):CountryDto?{
        return bandRepository.findCountryByBandId(bandId)
    }
    fun getBandData(bands:List<Band>):List<BandDto>{
        return bands.map{ b->
            BandDto(
                id=b.id,
                name=b.name,
                formedYear=b.formedYear,
                disbandedYear=b.disbandedYear,
                status=b.status,
                country=getBandsCountry(b.id!!),
                description=b.description
            )

        }
    }
    //endregion
    //region query
    fun getAll():List<BandDto>{
        val bands=bandRepository.findAll()
        return getBandData(bands)
    }

    fun getBandDataById(id:Int):BandDto{
        val band=bandRepository.findById(id)
        return getBandData(listOf(band.get())).first()
    }

    fun getBandById(id:Int):Band{
        return bandRepository.findBandById(id)
    }

    fun getBandByIdWiki(id:Int):BandWikiDto {
        val dataRaw=bandRepository.findById(id)

        return BandWikiDto(
            name=dataRaw.get().name,
            formedYear=dataRaw.get().formedYear,
            disbandedYear=dataRaw.get().disbandedYear,
            status=dataRaw.get().status,
            country=getBandsCountry(dataRaw.get().id!!),
            description=dataRaw.get().description,
            imageUrl=dataRaw.get().imageUrl,
            computedGenres=genreService.getBandAlbumGenresList(dataRaw.get().id!!)
        )
    }

    fun getCountryByName(name:String):CountryDto?{
        val country=countryRepository.getCountryByName(name)
        return country.map {c->
            CountryDto(c.id,c.name)
        }.firstOrNull()
    }

    fun getBandsByName(name:String):List<BandDto>{
        val bands=bandRepository.findByNameContainingIgnoreCase(name)
        return getBandData(bands)
    }

    fun getBandsByNameExact(name:String):List<BandDto>{
        val bands=bandRepository.findByNameIgnoreCase(name)
        return getBandData(bands)
    }

    fun getBandsByCountry(name:String):List<BandDto>{
        val country=getCountryByName(name)
        val bands=bandRepository.findByCountry(country?.id!!)
        return getBandData(bands)
    }

    fun getBandsByCountryId(id:Int):List<BandDto>{
        val bands=bandRepository.findByCountry(id)
        return getBandData(bands)
    }

    fun getBandsByFoundedBetween(startYear:Int?,endYear:Int?):List<BandDto>{
        val start=startYear?:1900
        val end=endYear?:LocalDate.now().year
        val bands=bandRepository.findByFormedYearBetween(start,end)
        return getBandData(bands)
    }

    fun getBandsByStatus(status:Status):List<BandDto>{
        val bands=bandRepository.findByStatus(status)
        return getBandData(bands)
    }


    //todo: CALL WHEN ADDING/MODYFING ALBUM DATA
    @Transactional
    fun calculateBandsGenre(bandId:Int) {
        bandsGenreRepository.deleteByBandsId(bandId)
        val dataRaw=genreService.getBandAlbumGenresList(bandId)
        val genresList:MutableList<Pair<String,Byte>> = arrayListOf()

        dataRaw.forEach {d->
            val cgenre=genreRepository.findGenreById(d.id!!)
            bandsGenreRepository.save(BandsGenres().apply {
                bands=getBandById(bandId)
                genre=cgenre
                importance=d.value
            })
            genresList.add(Pair(cgenre.properties!!,d.value!!))
        }

        val band=bandRepository.findBandById(bandId)
        band.averageGenre=GenreSimilarity.getAverageGenre(genresList)
        bandRepository.save(band)
    }

    /*@Scheduled(fixedRate=24*60*60*1000)
    @Transactional
    fun temp() {
        val bands=listOf(21,22,24,27,28,29)
        bands.forEach {calculateBandsGenre(it)}
    }*/


   fun getBandsGenre(id:Int):String{
       val data=bandRepository.findBandById(id)
       return data.averageGenre!!
   }

    fun getSimilarBands(bandId:Int,count:Int):List<BandGenreDto> {
        val dataRaw=bandRepository.findBandsWithAvgGenre()
        val avgGenre=getBandsGenre(bandId)
        val similarList:MutableList<Pair<Double,Band>> =arrayListOf()

        dataRaw.forEach {d->
            val similarity=GenreSimilarity.getGenreSimilarity(d.averageGenre!!,avgGenre)
            similarList.add(Pair(similarity,d))
        }

        similarList.removeIf {it.second.id==bandId}
        val mostSimilar=similarList.sortedBy {it.first}.take(count)

        return mostSimilar.map {
            BandGenreDto().apply {
                id=it.second.id!!
                name=it.second.name!!
                formedYear=it.second.formedYear!!
                country=CountryDto().apply {
                    id=it.second.country!!
                    name=countryRepository.getCountryNameById(it.second.country)
                }
                similarity=it.first
            }
        }
    }
    //endregion

    @Transactional
    fun addBandRequest(bandAddDto:BandAddDto,userLogin:String):Boolean{
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

        bandRepository.save(Band().apply {
            name=bandAddDto.name
            formedYear=bandAddDto.formedYear
            status=bandAddDto.status
            disbandedYear=bandAddDto.disbandedYear
            country=bandAddDto.country
            description=bandAddDto.description
            imageUrl=bandAddDto.imageUrl
        })

        val bandId=bandRepository.findTopIdByName(bandAddDto.name!!)

        val lastChangeId=contributionRepository.findTopChangeId()

        val changes:List<Pair<String,String?>> =listOf(
            Pair("bandId",bandId.toString()),
            Pair("name",bandAddDto.name),
            Pair("formedYear",bandAddDto.formedYear.toString()),
            Pair("status",bandAddDto.status.toString()),
            Pair("disbandedYear",bandAddDto.disbandedYear.toString()),
            Pair("country",bandAddDto.country.toString()),
            Pair("description",bandAddDto.description.toString()),
            Pair("imageUrl",bandAddDto.imageUrl.toString()),
        )

        changes.forEach {
            if(it.second!=null){
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.create
                    changedTable="band"
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
    fun revertBandAddition(changeId:Int,confirmedUserLogin:String):Boolean {
        val confirmingUser:User=userService.getUserByLogin(confirmedUserLogin)!!
        val rank=confirmingUser.rank!!.id!!
        if(rank<10) return false
        val contributions=contributionRepository.getByChangeId(changeId)
        if(contributions.find { it.confirmed==true }!=null && rank<12) return false

        val bandId=contributions.find {it.changedColumn=="bandId"}?.newValue?.toInt()
        val name=contributions.find {it.changedColumn=="name"}?.newValue

        if(bandId!=null&&name!=null) {
            val band:Band=bandRepository.findBandById(bandId)
            bandRepository.delete(band)
        }
        else return false

        return true
    }

}