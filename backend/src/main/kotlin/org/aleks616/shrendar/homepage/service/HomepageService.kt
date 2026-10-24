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
import org.aleks616.shrendar.band.model.ArtistBandsDto
import org.aleks616.shrendar.band.model.BandGenreDto
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.Utils.titleCase
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.event.model.EventWikiDto
import org.aleks616.shrendar.event.repository.EventRepository
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.homepage.model.*
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HomepageService(
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
    private val artistRepository:ArtistRepository,
    private val contributionRepository:ContributionRepository,
    private val eventRepository:EventRepository,
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
                    country,
                    artist.artistImageUrl
                )
            )
        }
        if(allBirthdays.isEmpty()) return emptyList()
        return allBirthdays
    }
    fun getUpcomingFavoriteArtistsBirthdays(login:String):List<ArtistAnniversaryDto>{
        val allBirthdays=getUpcomingFavoriteBirthdaysAndDeaths(login)
        val showBirthdays:List<ArtistBirthdayDeathDateDto> =allBirthdays.filter{it.daysTillBirthday!! <= 7&&it.daysTillBirthday!=0}.sortedBy{it.daysTillBirthday}.take(4)+
            allBirthdays.filter{it.daysTillBirthday!! > 7&&it.daysTillBirthday < 60}.shuffled().take(5).sortedBy{it.daysTillBirthday}
        return showBirthdays.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=it.daysTillBirthday,
                yearsSince=it.birthDate!!.until(LocalDate.now()).years+1,
                country=it.country,
                imageUrl=it.imageUrl
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
                imageUrl=it.imageUrl
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
                    age=it.birthDate!!.until(LocalDate.now()).years+1,
                    ageAtDeath=it.birthDate!!.until(it.deathDate).years,
                    country=countryRepository.getCountryNameById(it.country),
                    imageUrl=it.artistImageUrl
                )
            }

        val showDeathAnniversaries:List<ArtistBirthdayDeathDateDto> =allDeathAnniversaries
            .filter{it.daysTillDeathAnniversary!! <= 7}.sortedBy{it.daysTillDeathAnniversary}.take(4)+
            allDeathAnniversaries.filter{it.daysTillDeathAnniversary!! > 7}.shuffled().take(4).sortedBy{it.daysTillDeathAnniversary}

        return showDeathAnniversaries.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.deathDate,
                daysTillAnniversary=it.daysTillDeathAnniversary,
                yearsSince=it.deathDate!!.until(LocalDate.now()).years+1,
                country=it.country,
                imageUrl=it.imageUrl
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
                age=it.birthDate!!.until(LocalDate.now()).years+1,
                ageAtDeath=null,
                country=countryRepository.getCountryNameById(it.country),
                imageUrl=it.artistImageUrl
            )
        }

        val showBirthdays:List<ArtistBirthdayDeathDateDto> =allBirthdays.filter{it.daysTillBirthday!! <= 7}.sortedBy{it.daysTillBirthday}.take(4)+
            allBirthdays.filter{it.daysTillBirthday!! > 7}.shuffled().take(4).sortedBy{it.daysTillBirthday}

        return showBirthdays.map{
            ArtistAnniversaryDto(
                id=it.id,
                name=it.name,
                anniversaryDate=it.birthDate,
                daysTillAnniversary=it.daysTillBirthday,
                yearsSince=it.birthDate!!.until(LocalDate.now()).years+1,
                country=it.country,
                imageUrl=it.imageUrl
            )
        }
    }
    fun getUpcomingFavoriteAlbumAnniversaries(login:String):List<AlbumAnniversaryDto>{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val favoriteBands=userBandRepository.findByUser(user).map{it.band}

        if(favoriteBands.isEmpty()) return emptyList()
        val favoriteAlbums:MutableList<Album> =mutableListOf()
        favoriteBands.forEach{band->
            favoriteAlbums.addAll(albumRepository.findByBandId(band.id))
        }
        val allAlbums:MutableList<AlbumAnniversaryDto> =mutableListOf()
        favoriteAlbums.forEach{album->
            val daysTill=Utils.getDaysTillNextAnniversary(album.releaseDate)
            val albumAge=album.releaseDate.until(LocalDate.now()).years
            allAlbums.add(
                AlbumAnniversaryDto(
                    id=album.id,
                    bandId=album.band.id,
                    bandName=album.band.name,
                    title=album.title,
                    releaseDate=album.releaseDate,
                    type=album.type.toString().titleCase(),
                    artworkUrl=album.artworkUrl,
                    age=albumAge+1,
                    daysTillAnniversary=daysTill,
                )
            )
        }
        return allAlbums.filter{it.daysTillAnniversary!=0&&it.daysTillAnniversary!! <= 7}
                   .sortedBy{it.daysTillAnniversary!!}.take(4)+
               allAlbums.filter{it.daysTillAnniversary!! > 7&&it.daysTillAnniversary < 30}.shuffled().take(5)
                   .sortedBy{it.daysTillAnniversary}
    }
    fun getUpcomingRandomAlbumAnniversaries():List<AlbumAnniversaryDto>{
        val albumsData:MutableList<Album> =albumRepository.findAlbumsByUpcomingAnniversaries()
        val allAlbums:MutableList<AlbumAnniversaryDto> =mutableListOf()
        albumsData.filter{it.releaseDate.year<LocalDate.now().year}.forEach {album->
            val daysTill=Utils.getDaysTillNextAnniversary(album.releaseDate)
            val albumAge=album.releaseDate.until(LocalDate.now()).years
            allAlbums.add(
                AlbumAnniversaryDto(
                    id=album.id,
                    bandId=album.band.id,
                    bandName=album.band.name,
                    title=album.title,
                    releaseDate=album.releaseDate,
                    type=album.type.toString().titleCase(),
                    artworkUrl=album.artworkUrl,
                    age=albumAge+1,
                    daysTillAnniversary=daysTill,
                )
            )
        }

        return allAlbums.filter{it.daysTillAnniversary!!<=7}.sortedBy{it.daysTillAnniversary!!}.take(6)+
               allAlbums.filter{it.daysTillAnniversary!!>7}.shuffled().take(2).sortedBy{it.daysTillAnniversary}
    }

    fun getRandomUpcoming():UpcomingAnniversariesDto {
        val albums=getUpcomingRandomAlbumAnniversaries()
        val birthdays=getUpcomingRandomArtistsBirthdays()
        val deathAnniversaries=getUpcomingRandomArtistsDeathAnniversaries()
        return UpcomingAnniversariesDto(
            birthdays,
            deathAnniversaries,
            albums
        )
    }

    fun getUpcoming(login:String):UpcomingAnniversariesDto{
        val albums=getUpcomingFavoriteAlbumAnniversaries(login)
        val birthdays=getUpcomingFavoriteArtistsBirthdays(login)
        val deathAnniversaries=getUpcomingFavoriteArtistsDeathAnniversaries(login)
        return UpcomingAnniversariesDto(
            birthdays,
            deathAnniversaries,
            albums
        )
    }
    //endregion

    //region today
    fun getTodayAnniversaries(login:String):HomepageMainDto{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val month=LocalDate.now().monthValue
        val day=LocalDate.now().dayOfMonth
        val favoriteArtists=userArtistRepository.findByUser(user)
        val favoriteBands=userBandRepository.findByUser(user)
        val favoriteAlbums:MutableList<Album> =mutableListOf()
        favoriteBands.map{it.band}.forEach{band->
            favoriteAlbums.addAll(albumRepository.findByBandId(band.id))
        }
        val favoriteAlbumsAnniversaries=albumService.getAlbumAnniversariesByDate(month,day)
            .filter{d->favoriteAlbums.map{it.id}.contains(d.id)}

        val favoriteArtistsBirthdays=artistService.getByBirthday(month,day)
            .filter{d->favoriteArtists.map{it.artist!!.id}.contains(d.id)}
        val favoriteArtistsDeathAnniversaries=artistService.getByDeathDate(month,day)
            .filter{d->favoriteArtists.map{it.artist!!.id}.contains(d.id)}
        val otherBandMembersBirthdays=getOtherBandMembersBirthdaysToday(login)
            .filterNot{d->favoriteArtistsDeathAnniversaries.map{it.id}.contains(d.id)}
        val otherBandMembersDeathAnniversaries=getOtherBandMembersDeathAnniversariesToday(login)
            .filterNot{d->favoriteArtistsDeathAnniversaries.map{it.id}.contains(d.id)}


        val recommendedAlbums:List<Album> =getRecommendedAlbums(user)
            .filter{it.releaseDate.monthValue==month&&it.releaseDate.dayOfMonth==day}
            .take(10)
            .shuffled()
            .toMutableList()

        val recommendedAlbumsAnniversaries:MutableList<AlbumByDateDto> =recommendedAlbums.map{
            AlbumByDateDto(
                id=it.id,
                band=BandDto(it.band.id,it.band.name),
                title=it.title,
                releaseDate=it.releaseDate,
                type=it.type.toString().titleCase(),
                importance=it.importance,
                yearsSince=it.releaseDate.until(LocalDate.now()).years,
                genre=it.genre,
                artworkUrl=it.artworkUrl
            )
        }.toMutableList()

        if(recommendedAlbumsAnniversaries.size < 4){
            val bandsIds:MutableList<Int> =mutableListOf()
            favoriteBands.map{it.band}.forEach{band->
                bandsIds.addAll(bandService.getSimilarBands(band.id,20).mapNotNull{it.id})
            }
            val others=albumService.getAlbumAnniversariesByDate(month,day)
                .filter{d-> d.band!!.id in bandsIds}
                .filterNot{d-> favoriteAlbums.map{it.id}.contains(d.id)}
            recommendedAlbumsAnniversaries.addAll(others.take(5))
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
                imageUrl=it.artistImageUrl
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
                imageUrl=it.artistImageUrl
            )
        }.filterNot{d->favoriteArtistsDeathAnniversaries.map{it.id}.contains(d.id)}
            .filterNot{d->otherBandMembersDeathAnniversaries.map{it.id}.contains(d.id)}

        val birthdaysCount=favoriteArtistsBirthdays.count()+otherBandMembersBirthdays.count()+recommendedArtistBirthdays.count()
        val randomArtistsBirthdays=artistService.getByBirthday(month,day)
            .filterNot{r->favoriteArtistsBirthdays.map{it.id}.contains(r.id)}
            .filterNot{r->otherBandMembersBirthdays.map{it.id}.contains(r.id)}
            .filterNot{r->recommendedArtistBirthdays.map{it.id}.contains(r.id)}
            .shuffled()
            .take(10-birthdaysCount.coerceIn(0,10))
        val recommendedArtistBirthdaysResult=recommendedArtistBirthdays+randomArtistsBirthdays

        val deathAnniversariesCount=favoriteArtistsDeathAnniversaries.count()+otherBandMembersDeathAnniversaries.count()+recommendedArtistsDeathAnniversaries.count()
        val randomArtistDeathAnniversaries=artistService.getByDeathDate(month,day)
            .filterNot{r->favoriteArtistsDeathAnniversaries.map{it.id}.contains(r.id)}
            .filterNot{r->otherBandMembersDeathAnniversaries.map{it.id}.contains(r.id)}
            .filterNot{r->recommendedArtistsDeathAnniversaries.map{it.id}.contains(r.id)}
            .shuffled()
            .take(3-deathAnniversariesCount.coerceIn(0,3))

        val recommendedArtistDeathdaysResult=recommendedArtistsDeathAnniversaries+randomArtistDeathAnniversaries

        return HomepageMainDto(
            favoriteAlbumsAnniversaries,
            favoriteArtistsBirthdays,
            favoriteArtistsDeathAnniversaries,
            otherBandMembersBirthdays,
            otherBandMembersDeathAnniversaries,
            recommendedArtistBirthdaysResult,
            recommendedArtistDeathdaysResult,
            recommendedAlbumsAnniversaries,
            getEventAnniversariesToday()
        )

    }

    fun getTodayAnniversariesNoAuth():HomepageMainDto{
        val month=LocalDate.now().monthValue
        val day=LocalDate.now().dayOfMonth

        val albumsRaw=albumRepository.findByReleaseDateMonthAndDay(month,day).shuffled().take(6)
        val albums=albumsRaw.map{
            AlbumByDateDto(
                id=it.id,
                band=BandDto(it.band.id,it.band.name),
                title=it.title,
                releaseDate=it.releaseDate,
                type=it.type.toString().titleCase(),
                importance=it.importance,
                yearsSince=it.releaseDate.until(LocalDate.now()).years,
                genre=it.genre,
                artworkUrl=it.artworkUrl
            )
        }
        val artistsBirthdays=artistService.getByBirthday(month,day).shuffled().take(6)
        val artistsDeathAnniversaries=artistService.getByDeathDate(month,day).shuffled().take(6)

        return HomepageMainDto(
            albums,
            artistsBirthdays,
            artistsDeathAnniversaries,
            null,
            null,
            null,
            null,
            null,
            getEventAnniversariesToday()
        )
    }

    fun getOtherBandMembers(login:String):List<BandsMembers>{
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val favoriteBands=userBandRepository.findByUser(user)
        val bandsMembers:MutableList<BandsMembers> =mutableListOf()
        favoriteBands.map{it.band}.forEach{band->
            bandsMembers.addAll(bandsMemberRepository.findByBandId(band.id))
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
                    imageUrl=it.artist?.artistImageUrl
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
                    imageUrl=it.artist?.artistImageUrl
                )
            }
    }

    fun getEventAnniversariesToday():List<EventWikiDto>{
        val month=LocalDate.now().monthValue
        val day=LocalDate.now().dayOfMonth
        val eventData=eventRepository.findEventsByAnniversary(month,day)
        return eventData.filter{it.date!=null}.map{d->
            EventWikiDto(
                id=d.id,
                bandId=d.band.id,
                bandName=d.band.name,
                date=d.date,
                name=d.name,
                description=d.description,
                yearsSince=d.date?.until(LocalDate.now())?.years
            )
        }.shuffled().take(5)
    }

    fun getRecommendedAlbums(user:User):List<Album>{
        val favoriteBands=userBandRepository.findByUser(user)
        val favoriteAlbums:MutableList<Album> =mutableListOf()
        favoriteBands.map{it.band}.forEach{band->
            favoriteAlbums.addAll(albumRepository.findByBandId(band.id))
        }

        val favoriteGenresRaw:MutableList<Genre> =userGenreRepository.findByUser(user).map{it.genre}.toMutableList()
        val albumsByGenreRaw:MutableList<Album> =mutableListOf()

        favoriteGenresRaw.forEach{genre->
            albumsByGenreRaw.addAll(albumRepository.findByGenre(genre).filter{it.genre!=null})
        }

        val favoriteGenres:MutableList<String?> =favoriteGenresRaw
            .filter{it.properties!=null}.distinctBy{it.id}
            .map{it.properties}.toMutableList()

        val allGenres:MutableList<Genre> =genreRepository.findAll()
        favoriteBands.map{it.band}.forEach{band->
            if(allGenres.any{it.properties==band.averageGenre})
                favoriteGenres.add(band.averageGenre!!)
        }

        return albumsByGenreRaw.shuffled()
    }

    /** this is different from "other band members", this is recommended bands' band members **/
    fun getRecommendedArtists(user:User):List<Artist>{
        val favoriteBands=userBandRepository.findByUser(user)
        val recommendedBandsAll=mutableListOf<BandGenreDto>()
        favoriteBands.map{it.band}.forEach{band->
            recommendedBandsAll.addAll(bandService.getSimilarBands(band.id,10))
        }

        val recommendedBands=recommendedBandsAll.distinctBy{it.id}
        val artists:MutableList<Artist> =mutableListOf()
        recommendedBands.forEach{b->
            artists.addAll(bandsMemberRepository.findByBandId(b.id!!).map{it.artist!!})
        }
        return artists
    }
    //endregion

    /**
     * bands in which any of user's favorite artists played but isn't a favorite band
     * **/
    fun getCommonBands(login:String):CommonBandsDto {
        val user=userAccountService.getUserByLogin(login)?:throw IllegalArgumentException("User not found")
        val favoriteBands=userBandRepository.findByUser(user)
        val favoriteArtists=userArtistRepository.findByUser(user)
        val bandsMembers:MutableList<ArtistBandsDto> =mutableListOf()
        favoriteArtists.map{it.artist}.forEach{artist->
            bandsMembers.addAll(bandsMemberRepository.findBandsByArtistId(artist!!.id!!))
        }
        return CommonBandsDto(bandsMembers.filterNot { it.bandId in favoriteBands.map {f-> f.band.id} }.distinctBy { it.bandId })
    }

    fun getRecentlyAdded():List<RecentlyAddedDto>{
        val dataRaw=contributionRepository.getRecentAdditions(5)
        val result:MutableList<RecentlyAddedDto> =mutableListOf()
        dataRaw.groupBy { it.changeId }.forEach { d->
            println(d.key)
            val data=d.value.map {RecentlyAddedRow(
                id=it.id,
                changedColumn=it.changedColumn,
                oldValue=it.oldValue,
                newValue=it.newValue,
            )}

            result.add(RecentlyAddedDto(
                d.value[0].changeId,
                d.value[0].user.id,
                d.value[0].changedRecordId,
                d.value[0].changedTable,
                data,
                d.value[0].changedAt.toString()
            ))
        }

        return result
    }
}