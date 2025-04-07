package org.aleks616.shrendar.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name="albums",schema="Shrendar")
open class Albums {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="album_id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="band_id")
    open var bands:Bands?=null

    @Column(name="title",nullable=false,length=100)
    open var title:String?=null

    @Column(name="release_date")
    open var releaseDate:LocalDate?=null

    @Enumerated(EnumType.STRING)
    @Column(name="type", columnDefinition="ENUM('studio','EP','compilation','concert','demo','single','other')")
    open var type:AlbumType?=null
}

enum class AlbumType{
    studio,
    EP,
    compilation,
    concert,
    demo,
    single,
    other
}