package org.aleks616.shrendar.album.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.album.model.*
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.exception.InvalidAlbumImportanceException
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.security.RateLimiter
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
    private val genreService:GenreService
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
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("Invalid date")
        return albumService.getAlbumAnniversariesByDate(month,day)
    }

    //WIKI BAND PAGE 3/4
    @Throws(IllegalArgumentException::class)
    @GetMapping("/band/{bandId}")
    fun getAlbumsByBandId(@PathVariable bandId:Int):List<Album>{
        if(!albumService.doesBandExist(bandId)) throw IllegalArgumentException("Band doesn't exist")
        return albumService.getAlbumsByBandId(bandId)
    }

    @GetMapping("/band/like/{name}")
    fun getAlbumsByBandNameLike(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByBandName(name)
    }

    @Throws(IllegalArgumentException::class)
    @GetMapping("/year/{year}")
    fun getAlbumsByYear(@PathVariable year:Int):List<Album>{
        if(year>LocalDate.now().year || year<1918) throw IllegalArgumentException("Invalid year")
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(album.bandId==null||album.bandId<1||album.title.isNullOrEmpty()||album.type.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough data. At least band, title, and album type are required to add an album, and they should not be empty.")
        if(albumService.doesAlbumWithNameExistForBand(album))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This band already has an album with title ${album.title}. Edit the existing album instead. Check the contribution guide.")
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Album addition request received")
    }

    @PutMapping("/edit")
    fun editAlbum(@RequestBody album:AlbumAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(album.id==null||album.title==null||album.type==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Album id, title and type are required")
        if(!albumService.doesAlbumExist(album.id))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Album with id ${album.id} does not exist")
        if(albumService.doesAlbumWithNameExistForAlbumId(album))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This band already has an album with title ${album.title}. Do not repeat the same title if you're editing.")
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Album edit request received")
    }

    @DeleteMapping("/delete")
    fun deleteAlbum(@RequestParam id:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!albumService.doesAlbumExist(id))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Album with id $id does not exist")

        try{
            albumService.deleteAlbumRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Album deletion request received")
    }

    fun albumValidate(album:AlbumAddDto):ResponseEntity<String>?{
        if(album.bandId!=null&&!albumService.doesBandExist(album.bandId))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Band with id ${album.bandId} does not exist")
        if(album.type==AlbumType.studio&&(album.importance!=null&&album.importance !in 1..5))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Importance must be between 1 and 5 for studio albums.")
        if(album.type==AlbumType.EP&&(album.importance!=null&&album.importance !in 1..3))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Importance must be between 1 and 3 for EP albums.")
        if(album.type!=null&&album.type!=AlbumType.studio&&album.type!=AlbumType.EP&&album.importance!=null&&album.importance.toInt()!=0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Importance must be set to null or 0 for non-studio/EP albums")
        if(album.bandId!=null&&!albumService.isReleaseDateValid(album))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Album release date must be in the past or up to 1 year in the future and it can't be before year of band's formation")
        if(album.mainSubgenre!=null&&!genreService.doesGenreExist(album.mainSubgenre))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Genre with id ${album.mainSubgenre} does not exist")
        if(!Utils.isValidUrl(album.artworkUrl))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL is not valid or too long (maximum length is 255 characters)")
        return null
    }

}