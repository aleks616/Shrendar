package org.aleks616.shrendar.album.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.album.model.*
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.exception.InvalidAlbumImportanceException
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.userban.service.UserBanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/album")
class AlbumController (
    private val albumService:AlbumService,
    private val rateLimiter:RateLimiter,
    private val genreService:GenreService,
    private val userBanService:UserBanService
){
    //region query
    @GetMapping("/")
    fun getAll():List<AlbumDataDto>{
        return albumService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getAlbumById(@PathVariable id:Long):Album{
        return albumService.getById(id)
    }

    //WIKI ALBUM PAGE 1/1
    @GetMapping("wiki/{id}")
    fun getAlbumByIdWiki(@PathVariable id:Long):AlbumWikiDto{
        return albumService.getByIdWiki(id)
    }

    @Throws(IllegalArgumentException::class)
    @GetMapping("/inDate")
    fun getAlbumAnniversariesByDate(@RequestParam month:Int,@RequestParam day:Int):List<AlbumByDateDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("invalid_date")
        return albumService.getAlbumAnniversariesByDate(month,day)
    }

    //WIKI BAND PAGE 3/4
    @Throws(IllegalArgumentException::class)
    @GetMapping("/band/{bandId}")
    fun getAlbumsByBandId(@PathVariable bandId:Int):List<Album>{
        if(!albumService.doesBandExist(bandId)) throw IllegalArgumentException("band_not_exist")
        return albumService.getAlbumsByBandId(bandId)
    }

    @GetMapping("/band/like/{name}")
    fun getAlbumsByBandNameLike(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByBandName(name)
    }

    @Throws(IllegalArgumentException::class)
    @GetMapping("/year/{year}")
    fun getAlbumsByYear(@PathVariable year:Int):List<Album>{
        if(year>LocalDate.now().year || year<1918) throw IllegalArgumentException("invalid_year")
        return albumService.getAlbumsByYear(year)
    }

    @GetMapping("/like/{name}")
    fun getAlbumsByNameLike(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByName(name)
    }

    @GetMapping("/exact/{name}")
    fun getAlbumsByNameExact(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByNameExact(name)
    }

    //endregion

    @PostMapping("/add")
    fun addAlbum(@RequestBody album:AlbumAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned")
        if(album.bandId < 1||album.title.isEmpty()||album.type.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("insuffiecient_album_data")
        if(albumService.doesAlbumWithNameExistForBand(album))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("album_exists_already")
        if(albumValidate(album)!=null)
            return albumValidate(album)!!

        try {
            albumService.addAlbumRequest(album,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:InvalidAlbumImportanceException){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("album_addition_recieved")
    }

    @PutMapping("/edit")
    fun editAlbum(@RequestBody album:AlbumAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned")
        if(album.type==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_album_edit_data")
        if(!albumService.doesAlbumExist(album.id))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("album_id_not_exist")
        if(albumService.doesAlbumWithNameExistForAlbumId(album)) //todo review this (after changing patch to put)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("album_title_exists")
        if(albumValidate(album)!=null)
            return albumValidate(album)!!

        try{
            albumService.editAlbumRequest(album,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:InvalidAlbumImportanceException){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("album_edition_received")
    }

    @DeleteMapping("/delete")
    fun deleteAlbum(@RequestParam id:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned")
        if(!albumService.doesAlbumExist(id))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("album_id_not_exist")

        try{
            albumService.deleteAlbumRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("album_deletion_received")
    }

    fun albumValidate(album:AlbumAddDto):ResponseEntity<String>?{
        if(!albumService.doesBandExist(album.bandId))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("band_not_exist")
        if(album.type==AlbumType.STUDIO&&(album.importance!=null&&album.importance !in 0..5))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_importance_studio")
        if(album.type==AlbumType.EP&&(album.importance!=null&&album.importance !in 0..3))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_importance_ep")
        if(album.type!=AlbumType.STUDIO&&album.type!=AlbumType.EP&&album.importance!=null&&album.importance.toInt()!=0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_importance_other")
        if(!albumService.isReleaseDateValid(album))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_release_date")
        if(album.mainSubgenre!=null&&!genreService.doesGenreExist(album.mainSubgenre))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("genre_not_exist")
        if(!Utils.isValidUrl(album.artworkUrl))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("url_too_long")
        return null
    }

}