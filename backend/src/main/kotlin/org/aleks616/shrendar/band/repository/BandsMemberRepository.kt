package org.aleks616.shrendar.band.repository

import org.aleks616.shrendar.band.model.ArtistBandsDto
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.model.BandsMembersDataDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BandsMemberRepository :JpaRepository<BandsMembers,Int>{

    @Query("""
        SELECT bm.id, bm.artist.id, bm.artist.name, bm.band.id, bm.band.name, bm.role, bm.joinedYear, bm.leftYear, bm.nickname
        FROM BandsMembers bm JOIN Band b ON bm.band.id=b.id 
        JOIN Artist a ON a.id=bm.artist.id
        WHERE bm.band.id=:band
    """)
    fun findAllByBandName(band:Int):List<BandsMembersDataDto>

    @Query("""
        SELECT NEW org.aleks616.shrendar.band.model.ArtistBandsDto(bm.id, a.id, a.name, b.id, b.name, 
bm.role, bm.joinedYear, bm.leftYear, bm.nickname)
        FROM Band b JOIN BandsMembers bm ON b.id=bm.band.id
        JOIN Artist a ON bm.artist.id=a.id
        WHERE a.id=:id
    """)
    fun findBandsByArtistId(id:Long):List<ArtistBandsDto>

    @Query("""
        SELECT bm.id
        FROM BandsMembers bm
        WHERE bm.band.id=:bandId
        AND bm.artist.id=:artistId
        ORDER BY bm.id DESC
        LIMIT 1
    """)
    fun findTopIdByBandIdAndArtistId(bandId:Int,artistId:Long):Long


    @Query("""
        SELECT bm
        FROM BandsMembers bm
        WHERE bm.artist.id=:artistId
        AND bm.band.id=:bandId
    """)
    fun findArtistInBand(artistId:Long,bandId:Int):List<BandsMembers>
    fun findBandsMembersById(id:Long):BandsMembers
    fun existsById(id:Long):Boolean
    fun findById(id:Long):BandsMembers
    fun deleteById(id:Long)
    fun findByBandId(bandId:Int):MutableList<BandsMembers>


}