package org.aleks616.shrendar.contribution.service

import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service

@Service
class ContributionRevertService(
    private val userService:UserService,
    private val contributionRepository:ContributionRepository,
    private val albumRepository:AlbumRepository,
    private val artistRepository:ArtistRepository,
    private val bandRepository:BandRepository,
    private val bandsMemberRepository:BandsMemberRepository,
) {

    fun revertAddition(changeId:Int,confirmedUserLogin:String):Boolean {
        val confirmingUser:User=userService.getUserByLogin(confirmedUserLogin)!!
        val rank=confirmingUser.rank!!.id!!
        if(rank<10) return false
        val contributions=contributionRepository.getByChangeId(changeId)
        if(contributions.find {it.confirmed==true}!=null&&rank<12) return false

        val table=contributions[0].changedTable
        return when(table) {
            "album"->revertAlbumAddition(contributions)
            "artist"->revertArtistAddition(contributions)
            "band"->revertBandAddition(contributions)
            "bands_members"->revertBandMemberAddition(contributions)
            else->false
        }

    }

    fun revertAlbumAddition(contributions:List<Contribution>):Boolean {
        val bandId=contributions.find {it.changedColumn=="bandId"}?.newValue?.toInt()
        val title=contributions.find {it.changedColumn=="title"}?.newValue

        if(bandId!=null&&title!=null) {
            val album=albumRepository.findByBandId(bandId).find {it.title==title}
            if(album!=null) albumRepository.delete(album)
        }
        else return false

        return true
    }

    fun revertArtistAddition(contributions:List<Contribution>):Boolean {
        val artistId=contributions.find {it.changedColumn=="artistId"}?.newValue?.toInt()
        val name=contributions.find {it.changedColumn=="name"}?.newValue

        if(artistId!=null&&name!=null) {
            val artist:Artist=artistRepository.findArtistById(artistId)
            artistRepository.delete(artist)
        }
        else return false

        return true
    }

    fun revertBandAddition(contributions:List<Contribution>):Boolean {
        val bandId=contributions.find {it.changedColumn=="bandId"}?.newValue?.toInt()
        val name=contributions.find {it.changedColumn=="name"}?.newValue

        if(bandId!=null&&name!=null) {
            val band:Band=bandRepository.findBandById(bandId)
            bandRepository.delete(band)
        }
        else return false

        return true
    }

    fun revertBandMemberAddition(contributions:List<Contribution>):Boolean {
        val artistId=contributions.find {it.changedColumn=="artistId"}?.newValue?.toInt()
        val bandId=contributions.find {it.changedColumn=="bandId"}?.newValue?.toInt()
        val role=contributions.find {it.changedColumn=="role"}?.newValue
        val joinedYear=contributions.find {it.changedColumn=="joinedYear"}?.newValue?.toInt()

        if(artistId!=null&&bandId!=null&&joinedYear!=null&&role!=null) {
            val bandArtist:BandsMembers=bandsMemberRepository.findBandsMembersByDto(artistId,bandId,role,joinedYear)
            bandsMemberRepository.delete(bandArtist)
        }
        else return false

        return true
    }


}
