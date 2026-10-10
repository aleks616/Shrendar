package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.band.model.AlbumDto
import org.aleks616.shrendar.band.model.BandWikiDto
import org.aleks616.shrendar.band.model.BandsMembersWikiDto
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.common.Utils.titleCase
import org.aleks616.shrendar.genre.model.SimpleGenreDto
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.springframework.stereotype.Service

@Service
class BandWikiService(
    private val bandRepository:BandRepository,
    private val genreService:GenreService,
    private val bandService:BandService,
    private val bandsMemberService:BandsMemberService,
    private val albumRepository:AlbumRepository,
    private val userBandRepository:UserBandRepository
) {
    fun getBandByIdWiki(id:Int,userLogin:String?):BandWikiDto {
        if(!bandService.doesBandExist(id)) throw IllegalArgumentException("band_not_exist")
        val dataRaw=bandRepository.findBandById(id)
        val countryName=bandService.getBandsCountry(id)?.name?:"unknown"
        val genres:List<SimpleGenreDto> =genreService.getBandAlbumGenresList(id).map{
            SimpleGenreDto(
                id=it.id,
                name=it.name
            )
        }

        val isInFavorites=if(userLogin==null) false else userBandRepository.findByBandIdAndUser_Login(id,userLogin).isNotEmpty()

        return BandWikiDto(
            name=dataRaw.name,
            formedYear=dataRaw.formedYear,
            disbandedYear=dataRaw.disbandedYear,
            status=dataRaw.status.toString().titleCase(),
            country=countryName,
            description=dataRaw.description,
            imageUrl=dataRaw.imageUrl,
            computedGenres=genres,
            bandMembers=getAllBandMembersWiki(id),
            albums=getBandsAlbums(id),
            similar=bandService.getSimilarBands(id,8),
            isFavorite=isInFavorites
        )
    }

    fun getBandsAlbums(id:Int):List<AlbumDto>{
        val dataRaw=albumRepository.findByBandId(id)
        return dataRaw.map {AlbumDto(
            id=it.id,
            title=it.title,
            releaseDate=it.releaseDate,
            type=it.type.toString().titleCase(),
            importance=it.importance,
            genreId=it.genre?.id?:0,
            genreName=it.genre?.name?:"",
            artworkUrl=it.artworkUrl,
            description=it.description
        )}
    }

    fun getAllBandMembersWiki(id:Int):List<BandsMembersWikiDto>{
        val dataRaw=bandsMemberService.getAllBandMembers(id)
        val data=dataRaw.map {BandsMembersWikiDto(
            id=it.id,
            artistId=it.artistId,
            artistName=it.artistName,
            bandId=it.bandId,
            nickname=it.nickname,
            yearRole=it.yearRole
        )}
        return data.sortedBy { it.artistName }
    }
}
