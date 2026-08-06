package org.aleks616.shrendar.contribution.service

import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.RankTooLowToRevertConfirmedContribution
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service

@Service
class ContributionRevertService(
    private val albumRepository:AlbumRepository,
    private val artistRepository:ArtistRepository,
    private val bandRepository:BandRepository,
    private val bandService:BandService,
    private val bandsMemberRepository:BandsMemberRepository,
    private val contributionRepository:ContributionRepository,
    private val userService:UserService,
) {

    fun revertAddition(changeId:Int,confirmedUserLogin:String):Boolean {
        val confirmingUser:User=userService.getUserByLogin(confirmedUserLogin)!!
        val rank=confirmingUser.rank!!.id!!
        if(rank<10) return false
        val contributions=contributionRepository.getByChangeId(changeId)
        if(contributions[0].confirmed==true&&rank<12) throw RankTooLowToRevertConfirmedContribution("Rank 12 is required to revert confirmed contribution. User rank: $rank")

        val table=contributions[0].changedTable
        val type=contributions[0].action
        if(type==Action.create){
            return when(table) {
                "album"->revertAlbumAddition(contributions)
                "artist"->revertArtistAddition(contributions)
                "band"->revertBandAddition(contributions)
                "bands_members"->revertBandMemberAddition(contributions)
                else->throw IllegalArgumentException("table name has to be one of: album, artist, band, bands_members. actual: $table")
            }
        }
        else throw UnsupportedOperationException("reverting of edits and removal is not supported yet")
    }

    fun revertAlbumAddition(contributions:List<Contribution>):Boolean {
        val albumId=contributions[0].changedRecordId

        if(albumId!=null) {
            val album=albumRepository.findAlbumById(contributions[0].changedRecordId!!)
            albumRepository.delete(album)
            val bandId=contributions.find {it.changedColumn=="band_id"}?.newValue?.toInt()
            if(bandId!=null) bandService.calculateBandsGenre(bandId)
        }
        else return false

        return true
    }

    fun revertArtistAddition(contributions:List<Contribution>):Boolean {
        val artistId=contributions[0].changedRecordId

        if(artistId!=null) {
            val artist:Artist=artistRepository.findArtistById(artistId)
            artistRepository.delete(artist)
        }
        else return false

        return true
    }

    fun revertBandAddition(contributions:List<Contribution>):Boolean {
        val bandId=contributions[0].changedRecordId

        if(bandId!=null) {
            val band:Band=bandRepository.findBandById(bandId)
            bandRepository.delete(band)
        }
        else return false

        return true
    }

    fun revertBandMemberAddition(contributions:List<Contribution>):Boolean {
        val id=contributions[0].changedRecordId

        if(id!=null) {
            val bandArtist:BandsMembers=bandsMemberRepository.findBandsMembersById(id)
            bandsMemberRepository.delete(bandArtist)
        }
        else return false

        return true
    }


}
