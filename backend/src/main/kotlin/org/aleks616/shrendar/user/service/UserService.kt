package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.artist.model.FavoriteArtistDto
import org.aleks616.shrendar.band.model.FavoriteBandDto
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.genre.model.FavoriteGenreDto
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UserProfileDto
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.repository.UserLogRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UserService(
    private val userRepository:UserRepository,
    private val userBandRepository:UserBandRepository,
    private val userArtistRepository:UserArtistRepository,
    private val userGenreRepository:UserGenreRepository,
    private val contributionService:ContributionService,
    private val countryRepository:CountryRepository,
    private val userLogRepository:UserLogRepository,
    private val bandsMemberService:BandsMemberService,
) {

    fun getUserProfile(login:String,userLogin:String):UserProfileDto {
        val user:User=userRepository.findByLogin(login)?:throw IllegalArgumentException("User not found")
        if(user.deleted==true) return UserProfileDto(
            "deleted","deleted",0,"none",0,"deleted"
        )
        val favoriteBandsRaw=userBandRepository.findByUser(user)
        val favoriteBands:List<FavoriteBandDto> =favoriteBandsRaw.map {d->
            FavoriteBandDto(
                id=d.band.id,
                name=d.band.name,
                country=countryRepository.findById(d.band.country!!).get().name!!,
                activeYears="${d.band.formedYear} - ${d.band.disbandedYear?:""}"
            )
        }

        val favoriteArtistsRaw=userArtistRepository.findByUser(user)
        val favoriteArtists=favoriteArtistsRaw.map {d->
            FavoriteArtistDto(
                id=d.artist!!.id,
                name=d.artist!!.name,
                bands=bandsMemberService.getArtistBandsList(d.artist!!.id!!)
            )
        }

        val favoriteGenresRaw=userGenreRepository.findByUser(user)
        val favoriteGenres=favoriteGenresRaw.map {d->
            FavoriteGenreDto(
                id=d.genre.id,
                name=d.genre.name,
            )
        }

        val contributions=contributionService.getContributionsByRequestingUser(user.id)

        return UserProfileDto(
            user.login,
            user.username,
            user.rank.id,
            user.rank.name,
            user.xp,
            user.bio,
            timeSinceAccountCreated(user.id),
            timeSinceLogin(user.id),
            favoriteBands,
            favoriteArtists,
            favoriteGenres,
            contributions,
            userLogin==user.login
        )
    }

    fun timeSinceAccountCreated(userId:Int):String {
        val raw=userLogRepository.getUserLogById(userId)?.accountCreatedTime?:Instant.now()
        val now=Instant.now()
        val diff=ChronoUnit.DAYS.between(raw,now)
        val years=diff/365
        val months=diff%365/30
        val weeks=diff%365%30/7
        val days=diff%365%30%7
        return if(years>1) "$years time_Yp"
        else if(years>0) "$years time_Y"
        else if(months>1) "$months time_Mp"
        else if(months>0) "$months time_M"
        else if(weeks>1) "$weeks time_Wp"
        else if(weeks>0) "$weeks time_W"
        else if(days>1) "$days time_Dp"
        else "$days time_D"
    }

    fun timeSinceLogin(userId:Int):String {
        val raw=userLogRepository.getUserLogById(userId)?.lastLoginTime?:Instant.now()
        val now=Instant.now()
        val diff=ChronoUnit.DAYS.between(raw,now)

        val time=if(diff>365) diff/365 else if(diff>30) diff/30 else diff
        if(diff==0L) return "today"
        val unit:String=
            if(diff>730) "time_Yp"
            else if(diff>365) "time_Y"
            else if(diff>61) "time_Mp"
            else if(diff>30) "time_M"
            else if(diff>1) "time_Dp"
            else "time_D"

        return "$time $unit"
    }
}
