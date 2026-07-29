package org.aleks616.shrendar.album.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.album.model.*
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class AlbumService(
    private val albumRepository:AlbumRepository,
    private val contributionRepository:ContributionRepository,
    private val userService:UserService,
    private val bandRepository:BandRepository,
    private val genreRepository:GenreRepository,
    private val rankRepository:RankRepository,
    private val contributionService:ContributionService,
) {
    fun doesBandExist(bandId:Int):Boolean {
        return bandRepository.existsById(bandId)
    }

    //region query
    fun getAll():List<AlbumDataDto> {
        return albumRepository.findAll().map {
            AlbumDataDto(
                id=it.id,
                band=BandDto(id=it.band?.id,name=it.band?.name),
                title=it.title,
                releaseDate=it.releaseDate,
                type=it.type,
                importance=it.importance,
                genre=it.genre,
                artworkUrl=it.artworkUrl,
            )
        }
    }

    fun getById(id:Int):Album {
        return albumRepository.findAlbumById(id)
    }

    fun getByIdWiki(id:Int):AlbumWikiDto {
        val dataRaw=getById(id)
        val band=BandDto(dataRaw.band?.id,dataRaw.band?.name)
        val age=dataRaw.releaseDate!!.until(LocalDate.now()).years
        val daysTillAnniversary=Utils.getDaysTillNextAnniversary(dataRaw.releaseDate!!)

        return AlbumWikiDto(
            id=dataRaw.id,
            albumName=dataRaw.title,
            band=band,
            releaseDate=dataRaw.releaseDate,
            albumAge=age,
            daysTillAnniversary=daysTillAnniversary,
            type=dataRaw.type,
            genre=dataRaw.genre,
            description=dataRaw.description,
            artworkUrl=dataRaw.artworkUrl,
            importance=dataRaw.importance,
        )
    }

    fun getAlbumsByBandId(bandId:Int):List<Album> {
        val albums=albumRepository.findByBandId(bandId)
        return albums
    }

    fun getAlbumsByBandName(name:String):List<Album> {
        return albumRepository.findByBandNameContainingIgnoreCase((name))
    }

    fun getAlbumsByYear(year:Int):List<Album> {
        return albumRepository.findByYear(year)
    }

    fun getAlbumsByName(name:String):List<Album> {
        return albumRepository.findByTitleContainingIgnoreCase((name))
    }

    fun getAlbumsByNameExact(name:String):List<Album> {
        return albumRepository.findByTitleIgnoreCase((name))
    }

    fun getAlbumAnniversariesByDate(month:Int,day:Int):List<AlbumByDateDto> {
        val albumsInDate=getAll().filter {it.releaseDate?.monthValue==month&&it.releaseDate.dayOfMonth==day}
        val year=Calendar.getInstance().get(Calendar.YEAR)

        return albumsInDate.map {a->
            AlbumByDateDto(
                id=a.id,
                band=a.band?.let {BandDto(it.id,it.name)},
                title=a.title,
                releaseDate=a.releaseDate,
                type=a.type,
                importance=a.importance,
                yearsSince=year-(a.releaseDate?.year!!),
                genre=a.genre
            )

        }

    }
    //endregion


    @Transactional
    fun addAlbumRequest(albumAddDto:AlbumAddDto,userLogin:String):Boolean {
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val rankLimit=rankRepository.getRankById(requestingUser.rank!!.id!!).allowedContributions!!
        val recentContributionCount=contributionService.getContributionCountByUser(requestingUser.id!!)
        if(recentContributionCount>=rankLimit) return false

        val changes:List<Pair<String,String?>> =listOf(
        Pair("bandId",albumAddDto.bandId.toString()),
        Pair("title",albumAddDto.title),
        Pair("releaseDate",albumAddDto.releaseDate.toString()),
        Pair("type",albumAddDto.type.toString()),
        Pair("description",albumAddDto.description),
        Pair("mainSubgenre",albumAddDto.mainSubgenre.toString()),
        Pair("importance",albumAddDto.importance.toString()),
        Pair("artworkUrl",albumAddDto.artworkUrl),
        )

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        albumRepository.save(Album().apply {
            band=bandRepository.findById(albumAddDto.bandId!!).get()
            title=albumAddDto.title
            releaseDate=albumAddDto.releaseDate
            type=albumAddDto.type
            importance=albumAddDto.importance
            genre=genreRepository.findGenreById(albumAddDto.mainSubgenre!!)
            artworkUrl=albumAddDto.artworkUrl
            description=albumAddDto.description
        })

        val lastChangeId=contributionRepository.findTopChangeId()?:0
        changes.forEach {
            if(it.second!=null) {
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.create
                    changedTable="album"
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
        //todo notify mods or something
        return true
    }

    @Transactional
    fun editAlbumRequest(albumAddDto:AlbumAddDto,userLogin:String):Boolean {
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val rankLimit=rankRepository.getRankById(requestingUser.rank!!.id!!).allowedContributions!!
        val recentContributionCount=contributionService.getContributionCountByUser(requestingUser.id!!)
        if(recentContributionCount>=rankLimit) return false

        val album=albumRepository.findAlbumById(albumAddDto.id!!)

        val changes:MutableList<Triple<String,String?,String?>> =mutableListOf()

        if(albumAddDto.bandId!=null&&albumAddDto.bandId!=album.band?.id) {
            changes.add(Triple("bandId",album.band?.id?.toString(),albumAddDto.bandId.toString()))
            album.band=bandRepository.findById(albumAddDto.bandId).get()
        }
        if(albumAddDto.title!=null&&albumAddDto.title!=album.title) {
            changes.add(Triple("title",album.title,albumAddDto.title))
            album.title=albumAddDto.title
        }
        if(albumAddDto.releaseDate!=null&&albumAddDto.releaseDate!=album.releaseDate) {
            changes.add(Triple("releaseDate",album.releaseDate?.toString(),albumAddDto.releaseDate.toString()))
            album.releaseDate=albumAddDto.releaseDate
        }
        if(albumAddDto.type!=null&&albumAddDto.type!=album.type) {
            changes.add(Triple("type",album.type?.toString(),albumAddDto.type.toString()))
            album.type=albumAddDto.type
        }
        if(albumAddDto.description!=null&&albumAddDto.description!=album.description) {
            changes.add(Triple("description",album.description,albumAddDto.description))
            album.description=albumAddDto.description
        }
        if(albumAddDto.mainSubgenre!=null&&albumAddDto.mainSubgenre!=album.genre?.id) {
            changes.add(Triple("mainSubgenre",album.genre?.id?.toString(),albumAddDto.mainSubgenre.toString()))
            album.genre=genreRepository.findGenreById(albumAddDto.mainSubgenre)
        }
        if(albumAddDto.importance!=null&&albumAddDto.importance!=album.importance) {
            changes.add(Triple("importance",album.importance?.toString(),albumAddDto.importance.toString()))
            album.importance=albumAddDto.importance
        }
        if(albumAddDto.artworkUrl!=null&&albumAddDto.artworkUrl!=album.artworkUrl) {
            changes.add(Triple("artworkUrl",album.artworkUrl,albumAddDto.artworkUrl))
            album.artworkUrl=albumAddDto.artworkUrl
        }

        if(changes.isEmpty()) return false

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        albumRepository.save(album)

        val lastChangeId=contributionRepository.findTopChangeId()?:0
        changes.forEach {(column,oldValue,newValue)->
            contributionRepository.save(Contribution().apply {
                changeId=lastChangeId+1
                user=requestingUser
                action=Action.update
                changedTable="album"
                changedColumn=column
                changedRecordId=albumAddDto.id
                this.oldValue=oldValue
                this.newValue=newValue
                changedAt=time
                confirmed=trusted
                confirmedBy=confirmedByUser
            })
        }
        return true
    }

}