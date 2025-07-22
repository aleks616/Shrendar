package org.aleks616.shrendar.entities

import jakarta.persistence.*

@Entity
@Table(name="bands_genres",schema="Shrendar")
open class BandsGenres{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="band_id")
    open var bands:Bands?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="genre_id")
    open var genres:Genres?=null

    @Column(name="importance", columnDefinition="BIT(4)")
    open var importance:Int?=null
}