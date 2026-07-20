package org.aleks616.shrendar.band.service

/*
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random


@Service
class BandsMemberMockDataService(
    private val bandRepository:BandRepository,
    private val artistRepository:ArtistRepository,
    private val bandsMemberRepository:BandsMemberRepository
) {
    @Transactional
    fun populateMockData() {
        val bands=bandRepository.findAllById((32..1031).toList())
        val artistIds=(228..10222).toList()

        val roleConfigs=mapOf(
            "guitar" to 3,
            "drums" to 1,
            "bass" to 1,
            "vocals" to 1,
            "backing vocals" to 2
        )

        for(band in bands) {
            val formedYear=band.formedYear?:1900
            val disbandedYear=band.disbandedYear?:2026

            for((roleName,maxConcurrent) in roleConfigs) {
                val concurrentSlots=when(roleName) {
                    "guitar"-> {
                        val r=Random.nextDouble()
                        if(r<0.7) 1
                        else if(r<0.95) 2
                        else 3
                    }

                    "backing vocals"->Random.nextInt(0,maxConcurrent+1)
                    else->1
                }

                for(slot in 1..concurrentSlots) {
                    var currentYear=formedYear

                    while(currentYear<disbandedYear) {
                        if(Random.nextDouble()>0.8) {
                            currentYear+=Random.nextInt(1,3)
                            if(currentYear>=disbandedYear) break
                        }

                        val artist=findSuitableArtist(artistIds,currentYear,disbandedYear)?:break

                        val artistBirthYear=artist.birthDate?.year?:(currentYear-20)
                        val minJoinYear=maxOf(currentYear,artistBirthYear+12)

                        if(minJoinYear>=disbandedYear) {
                            currentYear++
                            continue
                        }

                        val stayDuration=Random.nextInt(1,11)
                        var leftYear:Int?=minJoinYear+stayDuration
                        if(leftYear!!>disbandedYear||Random.nextDouble()>0.6) {
                            leftYear=if(band.disbandedYear!=null) disbandedYear else null
                        }

                        val member=BandsMembers().apply {
                            this.band=band
                            this.artist=artist
                            this.role=roleName
                            this.joinedYear=minJoinYear
                            this.leftYear=leftYear
                        }
                        bandsMemberRepository.save(member)

                        if(Random.nextDouble()>0.8) {
                            val otherRoles=roleConfigs.keys.filter {
                                if(roleName=="vocals") it!="vocals"&&it!="backing vocals"
                                else if(roleName=="backing vocals") it!="vocals"&&it!="backing vocals"
                                else it!=roleName
                            }
                            if(otherRoles.isNotEmpty()) {
                                val secondRole=otherRoles[Random.nextInt(otherRoles.size)]

                                val overlapMember=BandsMembers().apply {
                                    this.band=band
                                    this.artist=artist
                                    this.role=secondRole
                                    this.joinedYear=minJoinYear
                                    this.leftYear=if(Random.nextDouble()>0.5) {
                                        val secondStay=Random.nextInt(1,15)
                                        minOf(disbandedYear,minJoinYear+secondStay)
                                    }
                                    else {
                                        leftYear
                                    }
                                }
                                bandsMemberRepository.save(overlapMember)
                            }
                        }

                        if(leftYear==null) break
                        currentYear=leftYear
                    }
                }
            }
        }
    }

    private fun findSuitableArtist(
        artistIds:List<Int>,
        minYear:Int,
        disbandedYear:Int
    ):org.aleks616.shrendar.artist.model.Artist? {
        var attempts=0
        while(attempts<50) {
            val id=artistIds[Random.nextInt(artistIds.size)]
            val artist=artistRepository.findById(id).orElse(null)?:continue
            val birthYear=artist.birthDate?.year?:(minYear-20)

            if(birthYear+12<disbandedYear) {
                return artist
            }
            attempts++
        }
        return null
    }
}
*/
