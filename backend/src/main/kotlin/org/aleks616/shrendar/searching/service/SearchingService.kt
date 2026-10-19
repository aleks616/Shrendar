package org.aleks616.shrendar.searching.service

import com.aallam.similarity.Levenshtein
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.event.repository.EventRepository
import org.aleks616.shrendar.searching.model.SearchResultItem
import org.aleks616.shrendar.searching.model.SearchResults
import org.aleks616.shrendar.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class SearchingService(
    private val albumRepository:AlbumRepository,
    private val artistRepository:ArtistRepository,
    private val bandRepository:BandRepository,
    private val eventRepository:EventRepository,
    private val userRepository:UserRepository
) {

    fun search(query:String):SearchResults {
        val levenshtein=Levenshtein()
        val results:MutableList<SearchResultItem> =mutableListOf()

        val bandsData=bandRepository.findBandsByNameContainsIgnoreCase(query)
        val bands=bandsData.map {
            SearchResultItem(
                name=it.name,
                description=it.description?.take(150)+"...",
                link="/band/${it.id}",
                type="band"
            )
        }
        results.addAll(bands.sortedBy {levenshtein.distance(it.name?.lowercase()?:"",query.lowercase())})

        val artistsData=artistRepository.findArtistsByNameContainsIgnoreCase(query)
        val artists=artistsData.map {
            SearchResultItem(
                name=it.name,
                description=it.description?.take(150)+"...",
                link="/artist/${it.id}",
                type="artist"
            )
        }
        results.addAll(artists.sortedBy {levenshtein.distance(it.name?.lowercase()?:"",query.lowercase())})

        val albumsData=albumRepository.findAlbumsByTitleContainsIgnoreCase(query)
        val albums=albumsData.map {
            SearchResultItem(
                name=it.title,
                description=it.description?.take(150)+"...",
                link="/album/${it.id}",
                type="album"
            )
        }
        results.addAll(albums.sortedBy {levenshtein.distance(it.name?.lowercase()?:"",query.lowercase())})

        val usersData=userRepository.findUsersByUsernameContainsIgnoreCaseOrLoginContainsIgnoreCase(query,query)
        val users=usersData.map {
            SearchResultItem(
                name=it.username,
                description=it.bio?.take(150)+"...",
                link="/u/${it.login}",
                type="user"
            )
        }
        results.addAll(users.sortedBy {levenshtein.distance(it.name?.lowercase()?:"",query.lowercase())})

        val eventsData=eventRepository.findEventsByNameContainsIgnoreCase(query)
        val events=eventsData.map {
            SearchResultItem(
                name=it.name,
                description=it.description?.take(150)+"...",
                link="/event/${it.id}",
                type="event"
            )
        }
        results.addAll(events.sortedBy {levenshtein.distance(it.name?.lowercase()?:"",query.lowercase())})

        return SearchResults(items=results)
    }

}
