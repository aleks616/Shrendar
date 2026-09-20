package org.aleks616.shrendar.userban.controller

import org.aleks616.shrendar.exception.RankTooLowException
import org.aleks616.shrendar.user.service.UserAccountService
import org.aleks616.shrendar.userban.model.BanDto
import org.aleks616.shrendar.userban.model.BansDto
import org.aleks616.shrendar.userban.model.UserBansDto
import org.aleks616.shrendar.userban.service.UserBanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api/ban")
class UserBanController(
    private val userBanService:UserBanService,
    private val userAccountService:UserAccountService
) {
    @GetMapping("/check/{userId}")
   fun isBanned(@PathVariable userId:Int):Boolean{
        if(userAccountService.doesUserExist(userId)) throw IllegalStateException("user_not_exist")
        return userBanService.isBanned(userId)
   }

    @GetMapping("/active")
    fun getActiveBans():List<BansDto>{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("user_not_exist")
        if(user.rank.id<10) throw RankTooLowException("You can't view this data")

        try{
            return userBanService.getActiveBans()
        }
        catch(e:Exception){
            throw IllegalStateException("something_wrong. ${e.message}")
        }
    }

    @GetMapping("/withAppeal")
    fun getBansWithAppeal():List<BansDto>{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("user_not_exist")
        if(user.rank.id<10) throw RankTooLowException("cant_view")

        try{
            return userBanService.getBansWithAppeal()
        }
        catch(e:Exception){
            throw IllegalStateException("something_wrong. ${e.message}")
        }
    }

    @GetMapping("/{userId}/all")
    fun getAllBansOfUser(@PathVariable userId:Int):UserBansDto{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("mod_not_exist")
        if(user.rank.id<10) throw RankTooLowException("cant_view")

        if(userAccountService.doesUserExist(userId)) throw IllegalStateException("user_not_exist")
        return userBanService.getAllBansOfUser(userId)
    }

    @GetMapping("/{userId}")
    fun getCurrentUserBanData(@PathVariable userId:Int){
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("mod_not_exist")
        if(user.rank.id<10) throw RankTooLowException("cant_view")

        if(userAccountService.doesUserExist(userId)) throw IllegalStateException("user_not_exist")
        try{
            userBanService.getCurrentUserBan(userId)
        }
        catch(e:Exception){
            throw IllegalStateException("something_wrong. ${e.message}")
        }
    }

    @GetMapping("/by/{modId}")
    fun getBansByMod(@PathVariable modId:Int):UserBansDto{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("mod_not_exist")
        if(user.rank.id<10) throw RankTooLowException("cant_view")

        if(userAccountService.doesUserExist(modId)) throw IllegalStateException("user_not_exist")
        return userBanService.getBansByMod(modId)
    }

    @PostMapping("/ban")
    fun banUser(@RequestBody data:BanDto):ResponseEntity<String>{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("user_not_exist")
        if(user.rank.id<10) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("page_not_found")

        if(data.userId==null||data.duration==null||data.description.isNullOrEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User id duration and description are required")
        if(userAccountService.doesUserExist(data.userId)) throw IllegalStateException("user_not_exist")

        userBanService.banUser(data,userLogin)
        return ResponseEntity.ok("user_banned")
    }

    @PostMapping("/appeal")
    fun appealBan(@RequestBody reason:String):ResponseEntity<String>{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        try{
            userBanService.appealBan(reason,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong. ${e.message}")
        }

        return ResponseEntity.ok("appeal_submitted")
    }

    @PostMapping("/cancel/{userId}")
    fun cancelBan(@PathVariable userId:Int):ResponseEntity<String>{
        val modAuth=SecurityContextHolder.getContext().authentication?:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong.")
        val modLogin=modAuth.name
        try{
            userBanService.cancelBan(userId,modLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong. ${e.message}")
        }

        return ResponseEntity.ok("ban_cancelled")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleBasicException(e:IllegalStateException):ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong. ${e.message}")
    }

    @ExceptionHandler(RankTooLowException::class)
    fun handleRankTooLowException():ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("page_not_found")
    }
}
