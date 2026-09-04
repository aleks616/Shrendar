package org.aleks616.shrendar.homepage.service

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumAnniversaryDto
import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.BandDto
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import org.aleks616.shrendar.artist.model.ArtistBirthdayDeathDateDto
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.BandGenreDto
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.homepage.model.HomePageMainDto
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HomePageService(
    private val albumService:AlbumService,
    private val artistService:ArtistService,
    private val bandsMemberRepository:BandsMemberRepository,
    private val userBandRepository:UserBandRepository,
    private val userAccountService:UserAccountService,
    private val countryRepository:CountryRepository,
    private val userArtistRepository:UserArtistRepository,
    private val albumRepository:AlbumRepository,
    private val userGenreRepository:UserGenreRepository,
    private val genreRepository:GenreRepository,
    private val bandService:BandService,
    private val artistRepository:ArtistRepository
){
    //region upcoming
    fun getUpcomingFavoriteBirthdaysAndDeaths(login:String):List<ArtistBirthdayDeathDateDto>{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val allBirthdays:MutableList<ArtistBirthdayDeathDateDto> =mutableListOf()
        val favoriteArtistsRaw=userArtistRepository.findByUser(user)
        val favoriteArtists=favoriteArtistsRaw.map{it.artist!!}
        if(favoriteArtists.isEmpty()) return emptyList()

        favoriteArtists.filter{it.birthDate!=null}.forEach{artist->
            val daysTillBirthday=Utils.getDaysTillNextAnniversary(artist.birthDate!!)
            var daysTillDeathAnn:Int?=null
            val age=artist.birthDate!!.until(LocalDate.now()).years
            var ageAtDeath:Int?=null
            if(artist.deathDate!=null){
                daysTillDeathAnn=Utils.getDaysTillNextAnniversary(artist.deathDate!!)
                ageAtDeath=artist.birthDate!!.until(artist.deathDate).years
            }

            val country:String?=if(artist.country!=null) countryRepository.getCountryNameById(artist.country) else null

            allBirthdays.add(
                ArtistBirthdayDeathDateDto(
                    artist.id,
                    artist.name,
                    artist.birthDate,
                    daysTillBirthday,
                    artist.deathDate,
                    daysTillDeathAnn,
                    age,
                    ageAtDeath,
                    country
                )
            )
        }
        if(allBirthdays.isEmpty()) return emptyList()
        return allBirthdays
    }
    fun getUpcomingFavoriteArtistsBirthdays(login:String):List<ArtistAnniversaryDto>{
        val allBirthdays=getUpcomingFavoriteBirthdaysAndDeaths(login)
        val showBirthdays:List<ArtistBirthdayDeathDateDto> =allBirthdays.filter{it.daysTillBirthday!! <= 7&&it.daysTillBirthday!=0}.sortedBy{it.daysTillBirthday}.take(4)+
            allBirthdays.filter{it.daysTillBirthday!! > 7&&it.daysTillBirthday < 60}.shuffled().take(4).sortedBy{it.daysTillBirthday}
        return showBirthdays.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=it.daysTillBirthday,
                yearsSince=it.birthDate!!.until(LocalDate.now()).years+1,
                country=it.country,
            )
        }
    }
    fun getUpcomingFavoriteArtistsDeathAnniversaries(login:String):List<ArtistAnniversaryDto>{
        val allDeathAnns=getUpcomingFavoriteBirthdaysAndDeaths(login).filter { it.daysTillDeathAnniversary!=null }
        if(allDeathAnns.isEmpty()) return emptyList()
        val showDeathAnns:List<ArtistBirthdayDeathDateDto> =allDeathAnns.filter{it.daysTillDeathAnniversary!! <= 7&&it.daysTillDeathAnniversary!=0}.sortedBy{it.daysTillDeathAnniversary}.take(4)+
                                                            allDeathAnns.filter{it.daysTillDeathAnniversary!! > 7&&it.daysTillDeathAnniversary < 60}.shuffled().take(2).sortedBy{it.daysTillDeathAnniversary}
        return showDeathAnns.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.deathDate,
                daysTillAnniversary=it.daysTillDeathAnniversary,
                yearsSince=it.deathDate!!.until(LocalDate.now()).years+1,
                country=it.country,
            )
        }
    }
    fun getUpcomingRandomArtistsDeathAnniversaries():List<ArtistAnniversaryDto>{
        val allDeathAnniversaries=artistRepository.findUpcomingDeathAnniversaries()
            .filter{it.deathDate!=null}.map{
                ArtistBirthdayDeathDateDto(
                    id=it.id,
                    name=it.name,
                    birthDate=it.birthDate,
                    daysTillBirthday=Utils.getDaysTillNextAnniversary(it.birthDate!!),
                    deathDate=it.deathDate,
                    daysTillDeathAnniversary=Utils.getDaysTillNextAnniversary(it.deathDate),
                    age=it.birthDate!!.until(LocalDate.now()).years,
                    ageAtDeath=it.birthDate!!.until(it.deathDate).years,
                    country=countryRepository.getCountryNameById(it.country),
                )
            }

        val showDeathAnniversaries:List<ArtistBirthdayDeathDateDto> =allDeathAnniversaries
            .filter{it.daysTillDeathAnniversary!! <= 7}.sortedBy{it.daysTillDeathAnniversary}.take(4)+
            allDeathAnniversaries.filter{it.daysTillDeathAnniversary!! > 7}.shuffled().take(2).sortedBy{it.daysTillDeathAnniversary}

        return showDeathAnniversaries.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=it.daysTillDeathAnniversary,
                yearsSince=it.deathDate!!.until(LocalDate.now()).years+1,
                country=it.country,
            )
        }
    }
    fun getUpcomingRandomArtistsBirthdays():List<ArtistAnniversaryDto>{
        val allBirthdays=artistRepository.findUpcomingBirthdays().map{
            ArtistBirthdayDeathDateDto(
                id=it.id,
                name=it.name,
                birthDate=it.birthDate,
                daysTillBirthday=Utils.getDaysTillNextAnniversary(it.birthDate!!),
                deathDate=it.deathDate,
                daysTillDeathAnniversary=Utils.getDaysTillNextAnniversary(it.deathDate),
                age=it.birthDate!!.until(LocalDate.now()).years,
                ageAtDeath=null,
                country=countryRepository.getCountryNameById(it.country),
            )
        }

        val showBirthdays:List<ArtistBirthdayDeathDateDto> =allBirthdays.filter{it.daysTillBirthday!! <= 7}.sortedBy{it.daysTillBirthday}.take(4)+
            allBirthdays.filter{it.daysTillBirthday!! > 7}.shuffled().take(2).sortedBy{it.daysTillBirthday}

        return showBirthdays.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=it.daysTillBirthday,
                yearsSince=it.birthDate!!.until(LocalDate.now()).years+1,
                country=it.country,
            )
        }
    }
    fun getUpcomingFavoriteAlbumAnniversaries(login:String):List<AlbumAnniversaryDto>{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val favoriteBands=userBandRepository.findByUser(user).map{it.band}

        if(favoriteBands.isEmpty()) return emptyList()
        val favoriteAlbums:MutableList<Album> =mutableListOf()
        favoriteBands.forEach{band->
            favoriteAlbums.addAll(albumRepository.findByBandId(band!!.id!!))
        }
        val allAlbums:MutableList<AlbumAnniversaryDto> =mutableListOf()
        favoriteAlbums.filter{it.releaseDate!=null}.forEach{album->
            val daysTill=Utils.getDaysTillNextAnniversary(album.releaseDate!!)
            val albumAge=album.releaseDate!!.until(LocalDate.now()).years
            allAlbums.add(
                AlbumAnniversaryDto(
                    id=album.id,
                    bandId=album.band!!.id,
                    bandName=album.band!!.name,
                    title=album.title,
                    releaseDate=album.releaseDate,
                    type=album.type,
                    artworkUrl=album.artworkUrl,
                    age=albumAge,
                    daysTillAnniversary=daysTill,
                )
            )
        }
        return allAlbums.filter{it.daysTillAnniversary!=0&&it.daysTillAnniversary!! <= 7}
                   .sortedBy{it.daysTillAnniversary!!}.take(4)+
               allAlbums.filter{it.daysTillAnniversary!! > 7&&it.daysTillAnniversary < 30}.shuffled().take(2)
                   .sortedBy{it.daysTillAnniversary}
    }

    fun getUpcomingRandomAlbumAnniversaries():List<AlbumAnniversaryDto>{
        val albumsData:MutableList<Album> =albumRepository.findAlbumsByUpcomingAnniversaries()
        val allAlbums:MutableList<AlbumAnniversaryDto> =mutableListOf()
        albumsData.forEach {album->
            val daysTill=Utils.getDaysTillNextAnniversary(album.releaseDate!!)
            val albumAge=album.releaseDate!!.until(LocalDate.now()).years
            allAlbums.add(
                AlbumAnniversaryDto(
                    id=album.id,
                    bandId=album.band!!.id,
                    bandName=album.band!!.name,
                    title=album.title,
                    releaseDate=album.releaseDate,
                    type=album.type,
                    artworkUrl=album.artworkUrl,
                    age=albumAge,
                    daysTillAnniversary=daysTill,
                )
            )
        }

        return allAlbums.filter{it.daysTillAnniversary!!<=7}.sortedBy{it.daysTillAnniversary!!}.take(6)+
               allAlbums.filter{it.daysTillAnniversary!!>7}.shuffled().take(2).sortedBy{it.daysTillAnniversary}
    }
    //endregion

    fun getTodayAnniversariesNoAuth():HomePageMainDto{
        val month=LocalDate.now().monthValue
        val day=LocalDate.now().dayOfMonth

        val albumsRaw=albumRepository.findByReleaseDateMonthAndDay(month,day).shuffled().take(6)
        val albums=albumsRaw.map{
            AlbumByDateDto(
                id=it.id,
                band=BandDto(it.band?.id,it.band?.name),
                title=it.title,
                releaseDate=it.releaseDate,
                type=it.type,
                importance=it.importance,
                yearsSince=it.releaseDate!!.until(LocalDate.now()).years,
                genre=it.genre,
                artworkUrl=it.artworkUrl
            )
        }
        val artistsBirthdays=artistService.getByBirthday(month,day).shuffled().take(6)
        val artistsDeathAnniversaries=artistService.getByDeathDate(month,day).shuffled().take(6)

        return HomePageMainDto(
            albums,
            artistsBirthdays,
            artistsDeathAnniversaries,
            null,
            null,
            null,
            null,
            null
        )
    }

    fun getOtherBandMembers(login:String):List<BandsMembers>{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val favoriteBands=userBandRepository.findByUser(user)
        val bandsMembers:MutableList<BandsMembers> =mutableListOf()
        favoriteBands.map{it.band}.forEach{band->
            bandsMembers.addAll(bandsMemberRepository.findByBandId(band!!.id!!))
        }
        return bandsMembers
    }

    fun getOtherBandMembersBirthdaysToday(login:String):List<ArtistAnniversaryDto>{
        val bandsMembers=getOtherBandMembers(login)
        return bandsMembers
            .filter{it.artist!!.birthDate!=null}
            .filter{it.artist!!.birthDate!!.monthValue==LocalDate.now().monthValue&&it.artist!!.birthDate!!.dayOfMonth==LocalDate.now().dayOfMonth}
            .map{
                ArtistAnniversaryDto(
                    id=it.artist!!.id!!,
                    name=it.artist!!.name!!,
                    anniversaryDate=it.artist!!.birthDate!!,
                    daysTillAnniversary=0,
                    yearsSince=it.artist!!.birthDate!!.until(LocalDate.now()).years,
                    country=countryRepository.getCountryNameById(it.artist!!.country),
                )
            }
            .distinctBy{it.id}
    }

    fun getOtherBandMembersDeathAnniversariesToday(login:String):List<ArtistAnniversaryDto>{
        val bandsMembers=getOtherBandMembers(login)
        return bandsMembers
            .filter{it.artist!!.deathDate!=null}
            .filter{it.artist!!.deathDate!!.monthValue==LocalDate.now().monthValue&&it.artist!!.deathDate!!.dayOfMonth==LocalDate.now().dayOfMonth}
            .map{
                ArtistAnniversaryDto(
                    id=it.artist!!.id!!,
                    name=it.artist!!.name!!,
                    anniversaryDate=it.artist!!.deathDate!!,
                    daysTillAnniversary=0,
                    yearsSince=it.artist!!.deathDate!!.until(LocalDate.now()).years,
                    country=countryRepository.getCountryNameById(it.artist!!.country),
                )
            }
    }

    fun getRecommendedAlbums(user:User):List<Album>{
        val favoriteBands=userBandRepository.findByUser(user)
        val favoriteAlbums:MutableList<Album> =mutableListOf()
        favoriteBands.map{it.band}.forEach{band->
            favoriteAlbums.addAll(albumRepository.findByBandId(band!!.id!!))
        }

        val favoriteGenresRaw:MutableList<Genre?> =userGenreRepository.findByUser(user).map{it.genre}.toMutableList()
        val albumsByGenreRaw:MutableList<Album> =mutableListOf()

        favoriteGenresRaw.forEach{genre->
            albumsByGenreRaw.addAll(albumRepository.findByGenre(genre!!).filter{it.id!=null&&it.genre!=null})
        }

        val favoriteGenres:MutableList<String?> =favoriteGenresRaw
            .filter{it?.properties!=null}.distinctBy{it?.id}
            .map{it?.properties}.toMutableList()

        val allGenres:MutableList<Genre> =genreRepository.findAll()
        favoriteBands.filter{it.band!=null}.map{it.band}.forEach{band->
            if(allGenres.any{it.properties==band?.averageGenre})
                favoriteGenres.add(band?.averageGenre!!)
        }

        return albumsByGenreRaw.shuffled()
    }

    /** this is different from "other band members", this is recommended bands' band members **/
    fun getRecommendedArtists(user:User):List<Artist>{
        val favoriteBands=userBandRepository.findByUser(user)
        val recommendedBandsAll=mutableListOf<BandGenreDto>()
        favoriteBands.map{it.band}.forEach{band->
            recommendedBandsAll.addAll(bandService.getSimilarBands(band?.id!!,10))
        }

        val recommendedBands=recommendedBandsAll.distinctBy{it.id}
        val artists:MutableList<Artist> =mutableListOf()
        recommendedBands.forEach{b->
            artists.addAll(bandsMemberRepository.findByBandId(b.id!!).map{it.artist!!})
        }
        return artists
    }

    fun getTodayAnniversaries(login:String):HomePageMainDto{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val month=LocalDate.now().monthValue
        val day=LocalDate.now().dayOfMonth
        val favoriteArtists=userArtistRepository.findByUser(user)
        val favoriteBands=userBandRepository.findByUser(user)
        val favoriteAlbums:MutableList<Album> =mutableListOf()
        favoriteBands.map{it.band}.forEach{band->
            favoriteAlbums.addAll(albumRepository.findByBandId(band!!.id!!))
        }
        val favoriteAlbumsAnniversaries=albumService.getAlbumAnniversariesByDate(month,day)
            .filter{d->favoriteAlbums.map{it.id}.contains(d.id)}
        val favoriteArtistsBirthdays=artistService.getByBirthday(month,day)
            .filter{d->favoriteArtists.map{it.artist!!.id}.contains(d.id)}
        val favoriteArtistsDeathAnniversaries=artistService.getByDeathDate(month,day)
            .filter{d->favoriteArtists.map{it.artist!!.id}.contains(d.id)}
        val otherBandMembersBirthdays=getOtherBandMembersBirthdaysToday(login)
            .filterNot{d->favoriteArtistsBirthdays.map{it.id}.contains(d.id)}
        val otherBandMembersDeathAnniversaries=getOtherBandMembersDeathAnniversariesToday(login)
            .filter{d->favoriteArtistsDeathAnniversaries.map{it.id}.contains(d.id)}


        val recommendedAlbums:List<Album> =getRecommendedAlbums(user)
            .filter{it.releaseDate!!.monthValue==month&&it.releaseDate!!.dayOfMonth==day}
            .take(10)
            .shuffled()
            .toMutableList()

        val recommendedAlbumAnniversaries:MutableList<AlbumByDateDto> =recommendedAlbums.map{
            AlbumByDateDto(
                id=it.id,
                band=BandDto(it.band?.id,it.band?.name),
                title=it.title,
                releaseDate=it.releaseDate,
                type=it.type,
                importance=it.importance,
                yearsSince=it.releaseDate!!.until(LocalDate.now()).years,
                genre=it.genre,
                artworkUrl=it.artworkUrl
            )
        }.toMutableList()

        if(recommendedAlbumAnniversaries.size<4){
            val bandsIds:MutableList<Int> =mutableListOf()
            favoriteBands.map{it.band}.forEach{band->
                bandsIds.addAll(bandService.getSimilarBands(band?.id!!,20).mapNotNull{it.id})
            }
            val others=albumService.getAlbumAnniversariesByDate(month,day)
                .filter{d-> d.band!!.id in bandsIds}
                .filterNot{d-> favoriteAlbums.map{it.id}.contains(d.id)}
            recommendedAlbumAnniversaries.addAll(others.take(5))
        }

        val allRecommendedArtists=getRecommendedArtists(user)
        val recommendedArtistsBirthdaysData=allRecommendedArtists
            .filter{it.birthDate!!.monthValue==month&&it.birthDate!!.dayOfMonth==day}
            .shuffled().take(8)
        val recommendedArtistsDeathAnniversariesData=allRecommendedArtists
            .filter{it.deathDate!=null}
            .filter{it.deathDate!!.monthValue==month&&it.deathDate!!.dayOfMonth==day}
            .shuffled().take(4)

        val recommendedArtistBirthdays=recommendedArtistsBirthdaysData.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=0,
                yearsSince=it.birthDate!!.until(LocalDate.now()).years,
                country=countryRepository.getCountryNameById(it.country),
            )
        }.filterNot{d->favoriteArtistsBirthdays.map{it.id}.contains(d.id)}
            .filterNot{d->otherBandMembersBirthdays.map{it.id}.contains(d.id)}

        val recommendedArtistsDeathAnniversaries=recommendedArtistsDeathAnniversariesData.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.deathDate,
                daysTillAnniversary=0,
                yearsSince=it.deathDate!!.until(LocalDate.now()).years,
                country=countryRepository.getCountryNameById(it.country),
            )
        }.filterNot{d->favoriteArtistsDeathAnniversaries.map{it.id}.contains(d.id)}
            .filterNot{d->otherBandMembersDeathAnniversaries.map{it.id}.contains(d.id)}

        return HomePageMainDto(
            favoriteAlbumsAnniversaries,
            favoriteArtistsBirthdays,
            favoriteArtistsDeathAnniversaries,
            otherBandMembersBirthdays,
            otherBandMembersDeathAnniversaries,
            recommendedArtistBirthdays,
            recommendedArtistsDeathAnniversaries,
            recommendedAlbumAnniversaries
        )

    }
}