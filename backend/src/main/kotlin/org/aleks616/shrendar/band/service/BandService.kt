package org.aleks616.shrendar.band.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.model.CountryDto
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.genre.service.GenreSimilarity
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersBands
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BandService(
    private val bandRepository:BandRepository,
    private val bandsGenreRepository:BandsGenreRepository,
    private val bandsMemberRepository:BandsMemberRepository,
    private val contributionRepository:ContributionRepository,
    private val countryRepository:CountryRepository,
    private val genreService:GenreService,
    private val userService:UserService,
    private val genreRepository:GenreRepository,
    private val rankService:RankService,
    private val userBandRepository:UserBandRepository,
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


    @Transactional
    fun calculateBandsGenre(bandId:Int) {
        bandsGenreRepository.deleteByBandsId(bandId)
        val dataRaw=genreService.getBandAlbumGenresList(bandId)
        val genresList:MutableList<Pair<String,Byte>> = arrayListOf()
        dataRaw.filter { it.name!=null && it.value!=null }.forEach {d->
            val cGenre=genreRepository.findGenreById(d.id!!)
            bandsGenreRepository.save(BandsGenres().apply {
                bands=getBandById(bandId)
                genre=cGenre
                importance=d.value
            })
            genresList.add(Pair(cGenre.properties!!,d.value!!))
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
    fun doesBandExist(bandId:Int):Boolean{
        return bandRepository.existsById(bandId)
    }

    @Transactional
    fun addBandRequest(bandAddDto:BandAddDto,userLogin:String){
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

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

        val lastChangeId=contributionRepository.findTopChangeId()?:0

        val changes:List<Pair<String,String?>> =listOf(
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
                    changedRecordId=bandId?.toLong()
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.CREATE
                    changedTable="band"
                    changedColumn=it.first
                    oldValue=null
                    newValue=it.second
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }

    }

    @Transactional
    fun editBandRequest(bandAddDto:BandAddDto,userLogin:String){
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val band=getBandById(bandAddDto.id!!)
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

        updateIfChanged("name",band.name,bandAddDto.name,{band.name=it})
        updateIfChanged("formed_year",band.formedYear,bandAddDto.formedYear,{band.formedYear=it})
        updateIfChanged("disbanded_year",band.disbandedYear,bandAddDto.disbandedYear,{band.disbandedYear=it})
        updateIfChanged("status",band.status,bandAddDto.status,{band.status=it})
        updateIfChanged("country",band.country,bandAddDto.country,{band.country=it})
        updateIfChanged("description",band.description,bandAddDto.description,{band.description=it})
        updateIfChanged("image_url",band.imageUrl,bandAddDto.imageUrl,{band.imageUrl=it})
        //averageGenre should be updated separately

        if(changes.isEmpty()) throw IllegalStateException("no changes found")

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        bandRepository.save(band)
        val lastChangeId=contributionRepository.findTopChangeId()?:0
        changes.forEach { (column,oldValue,newValue)->
            contributionRepository.save(Contribution().apply {
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.UPDATE
                changedTable="band"
                changedColumn=column
                changedRecordId=bandAddDto.id!!.toLong()
                this.oldValue=oldValue
                this.newValue=newValue
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }
    }

    @Transactional
    fun deleteBandRequest(id:Int,userLogin:String,log:Boolean=true){
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
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
            val band=getBandById(id)
            val changes:List<Triple<String,String?,String?>> =listOf(
                Triple("id",band.id.toString(),null),
                Triple("name",band.name,null),
                Triple("formed_year",band.formedYear.toString(),null),
                Triple("disbanded_year",band.disbandedYear.toString(),null),
                Triple("status",band.status.toString(),null),
                Triple("country",band.country.toString(),null),
                Triple("description",band.description,null),
                Triple("image_url",band.imageUrl,null)
            )

            val lastChangeId=contributionRepository.findTopChangeId()?:0
            changes.forEach {(column,oldValue,newValue)->
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.DELETE
                    changedTable="band"
                    changedColumn=column
                    changedRecordId=id.toLong()
                    this.oldValue=oldValue
                    this.newValue=newValue
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }

        if(trusted){
            bandRepository.deleteById(id)
        }

    }

    @Transactional
    fun toggleFavoriteBand(bandId:Int,login:String){
        val user=userService.getUserByLogin(login)?:throw IllegalStateException("User not found")
        val band=bandRepository.findBandById(bandId)
        val recordId=userBandRepository.findByBandAndUser(band,user)?.id?:-1

        if(recordId==-1L){
            userBandRepository.saveAndFlush(UsersBands().apply {
                this.user=user
                this.band=band
            })
        }
        else
            userBandRepository.deleteById(recordId)
    }

    fun doesSameMemberExist(member:ArtistBandAddDto):Boolean{
        val data=bandsMemberRepository.findArtistInBand(member.artistId!!,member.bandId!!)
        return data.any {it.artist!!.id==member.artistId&&it.band!!.id==member.bandId&&it.role==member.role&&(it.joinedYear==member.joinedYear||it.leftYear==member.leftYear)}
    }

    fun getBandMemberById(id:Int):BandsMembers{
        return bandsMemberRepository.findById(id).get()
    }

    fun doesBandMemberExist(id:Long):Boolean{
        return bandsMemberRepository.existsById(id)
    }
}