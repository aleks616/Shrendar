package org.aleks616.shrendar.user.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.aleks616.shrendar.genre.model.Genres

@Entity
@Table(name="user_genres",schema="Shrendar")
open class UserGenre {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="user_id")
    open var users:Users?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="genre_id")
    open var genres:Genres?=null
}