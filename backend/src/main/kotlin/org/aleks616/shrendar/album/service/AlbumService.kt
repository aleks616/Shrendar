package org.aleks616.shrendar.album.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.album.model.*
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.exception.InvalidAlbumImportanceException
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class AlbumService(
    private val albumRepository:AlbumRepository,
    private val bandService:BandService,
    private val contributionRepository:ContributionRepository,
    private val genreRepository:GenreRepository,
    private val userService:UserService,
    private val rankService:RankService,
) {
    fun doesBandExist(bandId:Int):Boolean {
        return bandService.doesBandExist(bandId)
    }

    fun doesAlbumExist(albumId:Long):Boolean {
        return albumRepository.existsById(albumId)
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

    fun getById(id:Long):Album {
        return albumRepository.findAlbumById(id)
    }

    fun getByIdWiki(id:Long):AlbumWikiDto {
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

    fun doesAlbumWithNameExistForBand(albumAddDto:AlbumAddDto):Boolean{
        val albums=getAlbumsByBandId(albumAddDto.bandId!!)
        return albums.any{it.title==albumAddDto.title}
    }

    /**
     * for editing, the only thing required in albumAddDto is record id and title
     * **/
    fun doesAlbumWithNameExistForAlbumId(albumAddDto:AlbumAddDto):Boolean{
        val album=albumRepository.findById(albumAddDto.id!!)
        val albums=getAlbumsByBandId(album.band?.id!!)
        return albums.any{it.title==albumAddDto.title}
    }

    fun isReleaseDateValid(album:AlbumAddDto):Boolean{
        if(album.releaseDate!=null&&album.releaseDate>LocalDate.now().plusYears(1)) return false
        val band=bandService.getBandById(album.bandId!!)
        return !(band.formedYear!=null&&(band.formedYear!!>album.releaseDate?.year!!))
    }

    @Transactional
    fun addAlbumRequest(albumAddDto:AlbumAddDto,userLogin:String) {
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val changes:List<Pair<String,String?>> =listOf(
            Pair("band_id",albumAddDto.bandId.toString()),
            Pair("title",albumAddDto.title),
            Pair("release_date",albumAddDto.releaseDate.toString()),
            Pair("type",albumAddDto.type.toString()),
            Pair("description",albumAddDto.description),
            Pair("genre_id",albumAddDto.mainSubgenre.toString()),
            Pair("importance",albumAddDto.importance.toString()),
            Pair("artwork_url",albumAddDto.artworkUrl),
        )

        val time=LocalDateTime.now()
        var trusted=false
        var confirmedByUser:Int?=null
        if(requestingUser.rank!!.id!!>9) {
            trusted=true
            confirmedByUser=requestingUser.id
        }

        val albumImportance=if(albumAddDto.type==AlbumType.studio||albumAddDto.type==AlbumType.EP) albumAddDto.importance else null

        albumRepository.save(Album().apply {
            band=bandService.getBandById(albumAddDto.bandId!!)
            title=albumAddDto.title
            releaseDate=albumAddDto.releaseDate
            type=albumAddDto.type
            importance=albumImportance
            genre=genreRepository.findGenreById(albumAddDto.mainSubgenre!!)
            artworkUrl=albumAddDto.artworkUrl
            description=albumAddDto.description
        })
        val albumRecordId=albumRepository.findIdByData(albumAddDto.bandId!!,albumAddDto.title!!)

        val lastChangeId=contributionRepository.findTopChangeId()?:0

        changes.forEach {
            if(it.second!=null) {
                contributionRepository.save(Contribution().apply {
                    changedRecordId=albumRecordId
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.create
                    changedTable="album"
                    changedColumn=it.first
                    oldValue=null
                    newValue=it.second
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }
        bandService.calculateBandsGenre(albumAddDto.bandId)

        //todo notify mods or something
    }

    @Transactional
    fun editAlbumRequest(albumAddDto:AlbumAddDto,userLogin:String) {
        val requestingUser:User=userService.getUserByLogin(userLogin)!!
        val exception:ContributionLimitExceededException?=rankService.checkRank(requestingUser)
        if(exception!=null) throw exception

        val album=albumRepository.findAlbumById(albumAddDto.id!!)
        val changes=mutableListOf<Triple<String,String?,String?>>()

        if(albumAddDto.importance!=null&&albumAddDto.importance>3&&album.type!=AlbumType.studio&&albumAddDto.type!=AlbumType.studio)
            throw InvalidAlbumImportanceException("If you're setting album's importance to 4 or 5, its type must be studio")
        if(albumAddDto.importance!=null&&albumAddDto.importance.toInt()!=0&&album.type!=AlbumType.studio&&albumAddDto.type!=AlbumType.studio&&album.type!=AlbumType.EP&&albumAddDto.type!=AlbumType.EP)
            throw InvalidAlbumImportanceException("If you're setting album's importance to above 0, its type must be studio or EP")

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

        updateIfChanged("band_id",album.band?.id,albumAddDto.bandId,{album.band=bandService.getBandById(it)})
        updateIfChanged("title",album.title,albumAddDto.title,{album.title=it})
        updateIfChanged("release_date",album.releaseDate,albumAddDto.releaseDate,{album.releaseDate=it})
        updateIfChanged("type",album.type,albumAddDto.type,{album.type=it})
        updateIfChanged("description",album.description,albumAddDto.description,{album.description=it})
        updateIfChanged("genre_id",album.genre?.id,albumAddDto.mainSubgenre,{album.genre=genreRepository.findGenreById(it)})
        updateIfChanged("importance",album.importance,albumAddDto.importance,{album.importance=it})
        updateIfChanged("artwork_url",album.artworkUrl,albumAddDto.artworkUrl,{album.artworkUrl=it})

        if(changes.isEmpty()) throw IllegalStateException("no changes found")

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

        if(changes.any{it.first=="genre_id"}){
            val bandId:Int=albumAddDto.bandId?:getById(albumAddDto.id).band!!.id!!
            bandService.calculateBandsGenre(bandId)
        }

    }

    @Transactional
    fun deleteAlbumRequest(albumId:Long,userLogin:String,log:Boolean=true) {
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
            val album=albumRepository.findAlbumById(albumId)
            val changes:List<Triple<String,String?,String?>> =listOf(
                Triple("id",album.id.toString(),null),
                Triple("band_id",album.band?.id.toString(),null),
                Triple("title",album.title,null),
                Triple("release_date",album.releaseDate.toString(),null),
                Triple("type",album.type.toString(),null),
                Triple("description",album.description,null),
                Triple("genre_id",album.genre?.id.toString(),null),
                Triple("importance",album.importance.toString(),null),
                Triple("artwork_url",album.artworkUrl,null),
            )

            val lastChangeId=contributionRepository.findTopChangeId()?:0
            changes.forEach {(column,oldValue,newValue)->
                contributionRepository.save(Contribution().apply {
                    changeId=lastChangeId+1
                    user=requestingUser
                    action=Action.delete
                    changedTable="album"
                    changedColumn=column
                    changedRecordId=id
                    this.oldValue=oldValue
                    this.newValue=newValue
                    changedAt=time
                    confirmed=trusted
                    confirmedBy=confirmedByUser
                })
            }
        }

        if(trusted){
            val bandId:Int=getById(albumId).band!!.id!!
            bandService.calculateBandsGenre(bandId)
            albumRepository.deleteById(albumId)
        }

    }

}