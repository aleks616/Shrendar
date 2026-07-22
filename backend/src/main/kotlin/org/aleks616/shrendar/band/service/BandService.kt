package org.aleks616.shrendar.band.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.genre.service.GenreSimilarity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BandService(
    private val bandRepository:BandRepository,
    private val countryRepository:CountryRepository,
    private val genreService:GenreService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val genreRepository:GenreRepository
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
        val genresList:MutableList<Pair<String,Int>> = arrayListOf()

        dataRaw.forEach {d->
            val cgenre=genreRepository.findGenreById(d.id!!).firstOrNull()
            bandsGenreRepository.save(BandsGenres().apply {
                bands=getBandById(bandId)
                genre=cgenre
                importance=d.value
            })
            genresList.add(Pair(cgenre?.properties!!,d.value!!))
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


}