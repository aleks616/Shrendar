package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.BandsMembersDataDto
import org.aleks616.shrendar.band.model.BandsMembersDataExtendedDto
import org.aleks616.shrendar.band.model.BandsMembersDto
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.springframework.stereotype.Service
import kotlin.collections.forEach

@Service
class BandsMemberService(
    val bandRepository:BandRepository,
    val artistRepository:ArtistRepository,
    val bandsMemberRepository:BandsMemberRepository
) {
    fun getBandMembersRaw(band:Int):List<BandsMembersDataDto> {
        return bandsMemberRepository.findAllByBandName(band)
    }

    fun getAllBandMembers(band:Int):List<BandsMembersDto> {
        val dataRaw=getBandMembersRaw(band)
        val data:List<BandsMembersDataExtendedDto> =dataRaw.map {d->
            BandsMembersDataExtendedDto(
                id=d.id,
                artistId=d.artistId,
                artistName=d.artistName,
                bandId=d.bandId,
                bandName=d.bandName,
                role=d.role,
                joinedYear=d.joinedYear,
                leftYear=d.leftYear,
                nickname=d.nickname,
                yearRole=mutableListOf(),
            )
        }

        val result:MutableList<BandsMembersDto> =mutableListOf()
        var found:Boolean

        data.forEach {d->
            found=false
            val left:String=if(d.leftYear==null) "" else d.leftYear.toString()
            val yearRole:String=if(d.joinedYear!=d.leftYear) ("${d.role} (${d.joinedYear}-${left})") else d.joinedYear.toString()

            result.forEach {r->
                if(r.artistId==d.artistId) {
                    found=true
                    r.yearRole?.add(yearRole)
                }
            }

            if(!found) {
                d.yearRole?.add(yearRole)
                result.add(
                    BandsMembersDto(
                        id=d.id,
                        artistId=d.artistId,
                        artistName=d.artistName,
                        bandId=d.bandId,
                        bandName=d.bandName,
                        yearRole=d.yearRole,
                        nickname=d.nickname
                    )
                )
            }
        }
        return result
    }

    fun getCurrentBandMembers(band:Int):List<BandsMembersDto> {
       val allData=getAllBandMembers(band)
        return allData.filter {d-> d.yearRole?.any {it.contains("-)")} ?: false}
    }


    fun getPastBandMembers(band:Int):List<BandsMembersDto> {
        val allData=getAllBandMembers(band)
        val currentData=getCurrentBandMembers(band)
        return allData.filter {d-> d.artistId !in currentData.map {it.artistId}}
    }

}


