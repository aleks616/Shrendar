package org.aleks616.shrendar.contribution.controller

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.security.JwtUtil
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContributionControllerTest {

    @Autowired
    private lateinit var mockMvc:MockMvc

    @Autowired
    private lateinit var contributionRepository:ContributionRepository

    @Autowired
    private lateinit var userRepository:UserRepository

    @Autowired
    private lateinit var rankRepository:RankRepository

    @Autowired
    private lateinit var artistRepository:ArtistRepository

    @Autowired
    private lateinit var bandRepository:BandRepository

    @Autowired
    private lateinit var albumRepository:AlbumRepository

    @Autowired
    private lateinit var bandsMemberRepository:BandsMemberRepository

    @Autowired
    private lateinit var rateLimiter:org.aleks616.shrendar.security.RateLimiter

    private lateinit var trustedToken:String
    private lateinit var newbieToken:String
    private lateinit var midRankToken:String

    @BeforeEach
    fun setup() {
        contributionRepository.deleteAll()
        bandsMemberRepository.deleteAll()
        albumRepository.deleteAll()
        bandRepository.deleteAll()
        artistRepository.deleteAll()
        userRepository.deleteAll()
        rankRepository.deleteAll()

        rankRepository.saveAndFlush(Rank().apply {id=1; name="Newbie"; minXp=0; allowedContributions=10})
        rankRepository.saveAndFlush(Rank().apply {id=10; name="Contributor"; minXp=500; allowedContributions=50})
        val trustedRank=
            rankRepository.saveAndFlush(Rank().apply {id=12; name="Trusted"; minXp=1000; allowedContributions=100})

        userRepository.saveAndFlush(User().apply {
            login="newbie"
            username="Newbie"
            email="newbie@example.com"
            passwordHash="hash"
            rank=rankRepository.findById(1).get()
            verified=true
        })

        userRepository.saveAndFlush(User().apply {
            login="contributor"
            username="Contributor"
            email="contributor@example.com"
            passwordHash="hash"
            rank=rankRepository.findById(10).get()
            verified=true
        })

        userRepository.saveAndFlush(User().apply {
            login="trusted"
            username="Trusted"
            email="trusted@example.com"
            passwordHash="hash"
            rank=trustedRank
            verified=true
        })

        newbieToken=JwtUtil.createToken("newbie")
        midRankToken=JwtUtil.createToken("contributor")
        trustedToken=JwtUtil.createToken("trusted")

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
    }

    @Test
    fun `confirm should succeed for rank 10 or higher`() {
        val changeId=100L
        val newbieUser=userRepository.findByLogin("newbie")!!
        contributionRepository.save(Contribution().apply {
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="artist"
            changedColumn="name"
            newValue="Test Artist"
            confirmed=false
            changedAt=LocalDateTime.now()
        })

        mockMvc.post("/api/contribution/confirm") {
            header("Authorization","Bearer $midRankToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isOk()}
        }

        val contributions=contributionRepository.getByChangeId(changeId)
        assertTrue(contributions.all {it.confirmed==true})
        assertEquals(userRepository.findByLogin("contributor")?.id,contributions[0].confirmedBy)
    }

    @Test
    fun `confirm should fail for rank below 10`() {
        val changeId=101L
        val newbieUser=userRepository.findByLogin("newbie")!!
        contributionRepository.save(Contribution().apply {
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="artist"
            changedColumn="name"
            newValue="Test Artist"
            confirmed=false
            changedAt=LocalDateTime.now()
        })

        mockMvc.post("/api/contribution/confirm") {
            header("Authorization","Bearer $newbieToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isInternalServerError()}
        }

        val contributions=contributionRepository.getByChangeId(changeId)
        assertTrue(contributions.all {it.confirmed==false})
    }

    @Test
    fun `revert artist addition should work`() {
        val artist=artistRepository.save(Artist().apply {name="To Be Reverted"})
        val changeId=200L
        val newbieUser=userRepository.findByLogin("newbie")!!

        contributionRepository.save(Contribution().apply {
            changedRecordId=artist.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="artist"
            changedColumn="artistId"
            newValue=artist.id.toString()
            confirmed=false
            changedAt=LocalDateTime.now()
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=artist.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="artist"
            changedColumn="name"
            newValue="To Be Reverted"
            confirmed=false
            changedAt=LocalDateTime.now()
        })

        mockMvc.post("/api/contribution/revert") {
            header("Authorization","Bearer $midRankToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isOk()}
        }

        assertTrue(artistRepository.findAll().none {it.name=="To Be Reverted"})
    }

    @Test
    fun `revert band addition should work`() {
        val band=bandRepository.save(Band().apply {name="Band to Revert"})
        val changeId=201L
        val newbieUser=userRepository.findByLogin("newbie")!!

        contributionRepository.save(Contribution().apply {
            changedRecordId=band.id?.toLong()
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="band"
            changedColumn="bandId"
            newValue=band.id.toString()
            confirmed=false
            changedAt=LocalDateTime.now()
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=band.id?.toLong()
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="band"
            changedColumn="name"
            newValue="Band to Revert"
            confirmed=false
            changedAt=LocalDateTime.now()
        })

        mockMvc.post("/api/contribution/revert") {
            header("Authorization","Bearer $midRankToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isOk()}
        }
        assertTrue(bandRepository.findAll().none {it.name=="Band to Revert"})
    }

    @Test
    fun `revert album addition should work`() {
        val band=bandRepository.save(Band().apply {name="Metallica"})
        val album=albumRepository.save(Album().apply {this.band=band; title="Album to Revert"})
        val changeId=202L
        val newbieUser=userRepository.findByLogin("newbie")!!

        contributionRepository.save(Contribution().apply {
            changedRecordId=album.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="album"
            changedColumn="bandId"
            newValue=band.id.toString()
            confirmed=false
            changedAt=LocalDateTime.now()
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=album.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="album"
            changedColumn="title"
            newValue="Album to Revert"
            confirmed=false
            changedAt=LocalDateTime.now()
        })

        mockMvc.post("/api/contribution/revert") {
            header("Authorization","Bearer $midRankToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isOk()}
        }
        assertTrue(albumRepository.findAll().none {it.title=="Album to Revert"})
    }

    @Test
    fun `revert band member addition should work`() {
        val artist=artistRepository.save(Artist().apply {name="Artist"})
        val band=bandRepository.save(Band().apply {name="Band"})
        val bm=bandsMemberRepository.save(BandsMembers().apply {
            this.artist=artist
            this.band=band
            role="Guitar"
            joinedYear=2000
        })
        val changeId=203L
        val newbieUser=userRepository.findByLogin("newbie")!!

        contributionRepository.save(Contribution().apply {
            changedRecordId=bm.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            confirmed=false
            changedAt=LocalDateTime.now()
            changedTable="bands_members"
            changedColumn="artistId"
            newValue=artist.id.toString()
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=bm.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            confirmed=false
            changedAt=LocalDateTime.now()
            changedTable="bands_members"
            changedColumn="bandId"
            newValue=band.id.toString()
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=bm.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            confirmed=false
            changedAt=LocalDateTime.now()
            changedTable="bands_members"
            changedColumn="role"
            newValue="Guitar"
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=bm.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            confirmed=false
            changedAt=LocalDateTime.now()
            changedTable="bands_members"
            changedColumn="joinedYear"
            newValue="2000"
        })

        mockMvc.post("/api/contribution/revert") {
            header("Authorization","Bearer $midRankToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isOk()}
        }
        assertTrue(bandsMemberRepository.findAll().isEmpty())
    }

    @Test
    fun `revert of confirmed contribution should require rank 12 or higher`() {
        val artist=artistRepository.save(Artist().apply {name="Confirmed Artist"})
        val changeId=300L
        val newbieUser=userRepository.findByLogin("newbie")!!

        contributionRepository.save(Contribution().apply {
            changedRecordId=artist.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="artist"
            changedColumn="artistId"
            newValue=artist.id.toString()
            confirmed=true
            changedAt=LocalDateTime.now()
        })
        contributionRepository.save(Contribution().apply {
            changedRecordId=artist.id
            this.changeId=changeId
            user=newbieUser
            action=Action.create
            changedTable="artist"
            changedColumn="name"
            newValue="Confirmed Artist"
            confirmed=true
            changedAt=LocalDateTime.now()
        })

        mockMvc.post("/api/contribution/revert") {
            header("Authorization","Bearer $midRankToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isForbidden()}
        }
        assertTrue(artistRepository.findAll().any {it.name=="Confirmed Artist"})

        mockMvc.post("/api/contribution/revert") {
            header("Authorization","Bearer $trustedToken")
            param("changeId",changeId.toString())
        }.andExpect {
            status {isOk()}
        }
        assertFalse(artistRepository.findAll().any {it.name=="Confirmed Artist"})
    }

    @Test
    fun `unauthorized access to revert should fail`() {
        mockMvc.post("/api/contribution/revert") {
            param("changeId","1")
        }.andExpect {
            status {isForbidden()}
        }
    }
}
