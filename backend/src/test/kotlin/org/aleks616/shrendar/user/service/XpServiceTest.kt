package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.io.File
import java.time.LocalDate

class XpServiceTest {

    private lateinit var userRepository:UserRepository
    private lateinit var rankRepository:RankRepository
    private lateinit var xpService:XpService

    private val lockFile=File("last-xp-update-date")

    @BeforeEach
    fun setUp() {
        userRepository=mock(UserRepository::class.java)
        rankRepository=mock(RankRepository::class.java)
        xpService=XpService(userRepository,rankRepository)
        if(lockFile.exists()) lockFile.delete()
    }

    @AfterEach
    fun tearDown() {
        if(lockFile.exists()) lockFile.delete()
    }

    @Test
    fun `increaseAllUsersXp should increase xp when date is new`() {
        val user=User().apply {login="user1"; xp=10}
        val user2=User().apply { login="user2" }
        val rank=Rank().apply {id=1; minXp=0; name="Newbie"}

        `when`(userRepository.findAll()).thenReturn(listOf(user,user2))
        `when`(rankRepository.findAll()).thenReturn(listOf(rank))

        xpService.increaseAllUsersXp()

        assertEquals(11,user.xp)
        verify(userRepository,times(2)).saveAll(anyList())
        assertTrue(lockFile.exists())
        assertEquals(LocalDate.now().toString(),lockFile.readText())
    }

    @Test
    fun `increaseAllUsersXp should not increase xp when date is same`() {
        lockFile.writeText(LocalDate.now().toString())
        val user=User().apply {login="user1"; xp=10}

        xpService.increaseAllUsersXp()

        assertEquals(10,user.xp)
        verify(userRepository,never()).findAll()
    }

    @Test
    fun `updateAllUsersRanks should assign correct ranks`() {
        val rank1=Rank().apply {id=1; minXp=0; name="Rank 1"}
        val rank2=Rank().apply {id=2; minXp=100; name="Rank 2"}
        val user1=User().apply {xp=50}
        val user2=User().apply {xp=150}
        val user3=User().apply {xp=100;rank=rank2}

        `when`(userRepository.findAll()).thenReturn(listOf(user1,user2,user3))
        `when`(rankRepository.findAll()).thenReturn(listOf(rank1,rank2))

        xpService.updateAllUsersRanks()

        assertEquals(rank1,user1.rank)
        assertEquals(rank2,user2.rank)
        verify(userRepository).saveAll(anyList())
    }

    @Test
    fun `increaseUserXp should update xp and rank`() {
        val rank1=Rank().apply {id=1; minXp=0; name="Rank 1"}
        val rank2=Rank().apply {id=2; minXp=50; name="Rank 2"}
        val user=User().apply {login="testUser"; xp=40; rank=rank1}

        `when`(userRepository.findByLogin("testUser")).thenReturn(user)
        `when`(rankRepository.findAll()).thenReturn(listOf(rank1,rank2))

        xpService.increaseUserXp("testUser",15)

        assertEquals(55,user.xp)
        assertEquals(rank2,user.rank)
        verify(userRepository,times(2)).save(user)
    }

    @Test
    fun `increaseUserXp shouldn't crash for not found xp`(){
        val rank1=Rank().apply {id=1; minXp=0; name="Rank 1"}
        val rank2=Rank().apply {id=2; minXp=13; name="Rank 2"}
        val user=User().apply {login="testUser"; rank=rank1}

        `when`(userRepository.findByLogin("testUser")).thenReturn(user)
        `when`(rankRepository.findAll()).thenReturn(listOf(rank1,rank2))
        xpService.increaseUserXp("testUser",15)

        assertEquals(15,user.xp)
        assertEquals(rank2,user.rank)
        verify(userRepository,times(2)).save(user)
    }

    @Test
    fun `increaseUserXp with negative amount should decrease xp`() {
        val rank1=Rank().apply {id=2; minXp=10; name="Rank 1"}
        val user=User().apply {login="testUser"; xp=40; rank=rank1}
        `when`(userRepository.findByLogin("testUser")).thenReturn(user)
        `when`(rankRepository.findAll()).thenReturn(listOf(rank1))

        xpService.increaseUserXp("testUser",-10)

        assertEquals(30,user.xp)
    }

    @Test
    fun `manualRankAssign should set exact rank and min xp`() {
        val rank=Rank().apply {id=5; minXp=500; name="Special"}
        val user=User().apply {login="admin"; xp=0}

        `when`(userRepository.findByLogin("admin")).thenReturn(user)
        `when`(rankRepository.findAll()).thenReturn(listOf(rank))

        xpService.manualRankAssign("admin",5)

        assertEquals(rank,user.rank)
        assertEquals(500,user.xp)
        verify(userRepository).save(user)
    }

    @Test
    fun `manualRankAssign should throw RuntimeException if user not found`() {
        `when`(userRepository.findByLogin("ghost")).thenReturn(null)
        assertThrows<RuntimeException> {
            xpService.manualRankAssign("ghost",100)
        }
        verifyNoInteractions(rankRepository)
    }

    @Test
    fun `manualRankAssign should throw IllegalArgumentException for non-existent rank`() {
        val user=User().apply {login="user1"; xp=100}
        `when`(userRepository.findByLogin("user1")).thenReturn(user)
        assertThrows<IllegalArgumentException> {
            xpService.manualRankAssign("user1",999)
        }
    }

    @Test
    fun `increaseUserXp should not crash if user not found`() {
        `when`(userRepository.findByLogin("ghost")).thenReturn(null)
        xpService.increaseUserXp("ghost",100)
        verify(userRepository,never()).save(any())
    }
}
