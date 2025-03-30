package org.aleks616.shrendar.entities

import jakarta.persistence.*

@Entity
@Table(name="user_genres",schema="Shrendar")
open class UserGenre {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    open var users:Users?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="genre_id")
    open var genres:Genres?=null
}