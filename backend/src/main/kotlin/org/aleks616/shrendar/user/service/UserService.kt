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

    fun getUserProfile(login:String):UserProfileDto {
        val user:User=userRepository.findByLogin(login)?:throw IllegalArgumentException("User not found")
        val favoriteBandsRaw=userBandRepository.findByUser(user)
        val favoriteBands:List<FavoriteBandDto> =favoriteBandsRaw.map {d->
            FavoriteBandDto(
                id=d.band!!.id,
                name=d.band!!.name,
                country=countryRepository.findById(d.band!!.country!!).get().name!!
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
                id=d.genre!!.id,
                name=d.genre!!.name,
            )
        }

        val contributions=contributionService.getContributionsByRequestingUser(user.id!!)

        return UserProfileDto(
            user.login,
            user.username,
            user.rank!!.id,
            user.rank!!.name,
            user.bio,
            timeSinceAccountCreated(user.id!!),
            timeSinceLogin(user.id!!),
            favoriteBands,
            favoriteArtists,
            favoriteGenres,
            contributions
        )
    }

    fun timeSinceAccountCreated(userId:Int):String {
        val raw=userLogRepository.getUserLogById(userId)?.accountCreatedTime?:Instant.now()
        val now=Instant.now()
        val diff=ChronoUnit.DAYS.between(raw,now)
        val years=diff/365
        val months=diff%365/30
        val days=diff%365%30
        return if(years>0) "$years year $months months"
        else if(months>0) "$months months $days days"
        else "$days days"
    }

    fun timeSinceLogin(userId:Int):String {
        val raw=userLogRepository.getUserLogById(userId).lastLoginTime?:Instant.now()
        val now=Instant.now()
        val diff=ChronoUnit.DAYS.between(raw,now)

        val time=if(diff>365) diff/365 else if(diff>30) diff%30 else diff
        val unit:String=if(diff>365) "years" else if(diff>30) "months" else if(diff==0L) "today" else "days"
        return "$time $unit ago"
    }
}
