package org.aleks616.shrendar.event.service

import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.event.model.Event
import org.aleks616.shrendar.event.model.EventAddDto
import org.aleks616.shrendar.event.repository.EventRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.LocalDate

class EventServiceTest {
    private lateinit var eventRepository:EventRepository
    private lateinit var bandService:BandService
    private lateinit var contributionRepository:ContributionRepository
    private lateinit var userAccountService:UserAccountService
    private lateinit var rankService:RankService
    private lateinit var eventService:EventService
    private lateinit var requestingUser:User
    private lateinit var band:Band
    private lateinit var event:Event

    @BeforeEach
    fun setup() {
        eventRepository=mock(EventRepository::class.java)
        bandService=mock(BandService::class.java)
        contributionRepository=mock(ContributionRepository::class.java)
        userAccountService=mock(UserAccountService::class.java)
        rankService=mock(RankService::class.java)
        eventService=EventService(eventRepository,bandService,contributionRepository,userAccountService,rankService)

        band=Band().apply {id=2; name="Metallica"}
        requestingUser=User().apply {
            id=7
            login="tester"
            rank=Rank().apply {id=1}
        }
        event=Event().apply {
            id=1
            this.band=band
            date=LocalDate.of(2020,1,1)
            name="Concert"
            description="Description"
        }
    }

    @Test
    fun `doesEventExist should delegate to repository`() {
        `when`(eventRepository.existsById(1)).thenReturn(true)

        assertTrue(eventService.doesEventExist(1))
        verify(eventRepository).existsById(1)
    }

    @Test
    fun `addEventRequest should throw contribution limit exception`() {
        val limit=ContributionLimitExceededException("limit")
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(limit)

        assertThrows<ContributionLimitExceededException> {
            eventService.addEventRequest(
                EventAddDto(bandId=2,date=LocalDate.of(2020,1,1),name="Concert"),
                "tester"
            )
        }
        verifyNoInteractions(eventRepository,contributionRepository,bandService)
    }

    @Test
    fun `addEventRequest should save untrusted event and changes`() {
        val dto=EventAddDto(
            bandId=2,
            date=LocalDate.of(2020,1,1),
            name="Concert",
            description="Description"
        )
        stubAddDependencies(dto)
        `when`(eventRepository.save(any(Event::class.java))).thenAnswer {
            (it.arguments[0] as Event).apply { id=11 }
        }
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)

        eventService.addEventRequest(dto,"tester")

        val saved=ArgumentCaptor.forClass(Event::class.java)
        verify(eventRepository).save(saved.capture())
        assertEquals(dto.date,saved.value.date)
        assertEquals(dto.name,saved.value.name)
        assertEquals(dto.description,saved.value.description)
        verify(contributionRepository,times(4)).save(any(Contribution::class.java))
    }

    @Test
    fun `addEventRequest should mark trusted user changes confirmed`() {
        requestingUser.rank=Rank().apply {id=10}
        val dto=EventAddDto(bandId=2,date=LocalDate.of(2020,1,1),name="Concert")
        stubAddDependencies(dto)
        `when`(eventRepository.save(any(Event::class.java))).thenAnswer {
            (it.arguments[0] as Event).apply { id=11 }
        }

        eventService.addEventRequest(dto,"tester")

        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(saved.capture())
        assertTrue(saved.allValues.all {it.confirmed==true&&it.confirmedBy==requestingUser.id})
    }

    @Test
    fun `addEventRequest should work when last change id is null`() {
        val dto=EventAddDto(bandId=2,date=LocalDate.of(2020,1,1),name="Concert")
        stubAddDependencies(dto)
        `when`(eventRepository.save(any(Event::class.java))).thenAnswer {
            (it.arguments[0] as Event).apply { id=11 }
        }
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)

        eventService.addEventRequest(dto,"tester")

        verify(contributionRepository,times(4)).save(any(Contribution::class.java))
    }

    @Test
    fun `editEventRequest should throw contribution limit exception`() {
        val limit=ContributionLimitExceededException("limit")
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(limit)

        assertThrows<ContributionLimitExceededException> {
            eventService.editEventRequest(EventAddDto(id=1,bandId=2,date=LocalDate.of(2020,1,1),name="Concert"),"tester")
        }
        verifyNoInteractions(eventRepository,contributionRepository,bandService)
    }

    @Test
    fun `editEventRequest should throw when there are no changes`() {
        stubEditDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)

        assertThrows<IllegalStateException> {
            eventService.editEventRequest(EventAddDto(id=1),"tester")
        }
        verify(eventRepository,never()).save(any(Event::class.java))
    }

    @Test
    fun `editEventRequest should update changed values`() {
        stubEditDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)
        `when`(bandService.getBandById(4)).thenReturn(Band().apply {id=4})

        val dto=EventAddDto(id=1,bandId=4,date=LocalDate.of(2021,1,1),name="New Concert",description="New Description")

        eventService.editEventRequest(dto,"tester")

        assertEquals(4,event.band?.id)
        assertEquals(LocalDate.of(2021,1,1),event.date)
        assertEquals("New Concert",event.name)
        assertEquals("New Description",event.description)
        verify(eventRepository).save(event)
        verify(contributionRepository,times(4)).save(any(Contribution::class.java))
    }

    @Test
    fun `editEventRequest should ignore same values when they are provided`() {
        stubEditDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)

        val dto=EventAddDto(id=1,bandId=2,date=LocalDate.of(2020,1,1),name="Concert",description="Description")

        assertDoesNotThrow {
            eventService.editEventRequest(dto,"tester")
        }
    }

    @Test
    fun `editEventRequest should mark trusted changes confirmed`() {
        requestingUser.rank=Rank().apply {id=10}
        stubEditDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)
        `when`(bandService.getBandById(4)).thenReturn(Band().apply {id=4})

        eventService.editEventRequest(EventAddDto(id=1,bandId=4,name="New Concert"),"tester")

        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(saved.capture())
        assertTrue(saved.allValues.all {it.confirmed==true&&it.confirmedBy==requestingUser.id})
    }

    @Test
    fun `editEventRequest should work when last change id is null`() {
        stubEditDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)
        `when`(bandService.getBandById(4)).thenReturn(Band().apply {id=4})

        eventService.editEventRequest(EventAddDto(id=1,bandId=4,name="New Concert"),"tester")

        verify(contributionRepository,times(2)).save(any(Contribution::class.java))
    }

    @Test
    fun `deleteEventRequest should throw contribution limit exception`() {
        val limit=ContributionLimitExceededException("limit")
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(limit)

        assertThrows<ContributionLimitExceededException> { eventService.deleteEventRequest(1,"tester") }
        verifyNoInteractions(eventRepository,contributionRepository,bandService)
    }

    @Test
    fun `deleteEventRequest should not delete untrusted users`() {
        stubDeleteDependencies()

        eventService.deleteEventRequest(1,"tester",false)

        verify(eventRepository,never()).deleteById(1)
        verifyNoInteractions(bandService)
    }

    @Test
    fun `deleteEventRequest should log and delete for trusted users`() {
        requestingUser.rank=Rank().apply {id=10}
        stubDeleteDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)
        `when`(contributionRepository.findTopChangeId()).thenReturn(1)

        eventService.deleteEventRequest(1,"tester")

        verify(eventRepository).deleteById(1)
        verify(contributionRepository,times(5)).save(any(Contribution::class.java))
    }

    @Test
    fun `deleteEventRequest should work when last change id is null`() {
        requestingUser.rank=Rank().apply {id=10}
        stubDeleteDependencies()
        `when`(eventRepository.findEventById(1)).thenReturn(event)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)

        eventService.deleteEventRequest(1,"tester")

        verify(eventRepository).deleteById(1)
        verify(contributionRepository,times(5)).save(any(Contribution::class.java))
    }

    private fun stubAddDependencies(dto:EventAddDto) {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandService.getBandById(dto.bandId!!)).thenReturn(band)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
    }

    private fun stubEditDependencies() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
    }

    private fun stubDeleteDependencies() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
    }
}
