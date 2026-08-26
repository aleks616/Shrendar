package org.aleks616.shrendar.genre.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.user.model.UsersGenres
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service

@Service
class GenreService(
    private val genreRepository:GenreRepository,
    private val userService:UserService,
    private val userGenreRepository:UserGenreRepository
){
    fun getAll():List<Genre>{
        return genreRepository.findAll()
    }

    fun getBandAlbumGenresList(id:Int):List<GenreDto>{
        val data= genreRepository.findBandAlbumGenresList(id)
        return data.map { d->
            GenreDto(
                id=d.id,
                name=d.name,
                value=d.value?.toInt()?.toByte()
            )
        }
    }

    fun doesGenreExist(id:Int):Boolean{
        return genreRepository.existsById(id)
    }

    @Transactional
    fun toggleFavoriteGenre(genreId:Int,login:String){
        val user=userService.getUserByLogin(login)?:throw IllegalStateException("User not found")
        val genre=genreRepository.findGenreById(genreId)
        val recordId=userGenreRepository.findByGenreAndUser(genre,user)?.id?:-1

        if(recordId==-1){
            userGenreRepository.saveAndFlush(UsersGenres().apply {
                this.user=user
                this.genre=genre
            })
        }
        else
            userGenreRepository.deleteById(recordId)
    }

}