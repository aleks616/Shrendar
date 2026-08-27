package org.aleks616.shrendar.album.model

import jakarta.persistence.*
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.genre.model.Genre
import java.time.LocalDate

@Entity
@Table(name="album",schema="Shrendar")
open class Album {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="album_id",nullable=false)
    open var id:Long?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="band_id")
    open var band:Band?=null

    @Column(name="title",nullable=false,length=100)
    open var title:String?=null

    @Column(name="release_date")
    open var releaseDate:LocalDate?=null

    @Enumerated(EnumType.STRING)
    @Column(name="type", columnDefinition="ENUM('STUDIO','EP','COMPILATION','CONCERT','DEMO','SINGLE','OTHER')")
    open var type:AlbumType?=null

    @Column(name="importance", columnDefinition="TINYINT")
    open var importance:Byte?=null

    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="genre_id")
    open var genre:Genre?=null

    @Column(name="artwork_url")
    open var artworkUrl:String?=null

    @Column(name="description", columnDefinition="TEXT")
    open var description:String?=null
}