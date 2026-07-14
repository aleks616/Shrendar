package org.aleks616.shrendar.band.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.aleks616.shrendar.artist.model.Artist

@Entity
@Table(name="bands_members",schema="Shrendar")
open class BandsMembers {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id: Int? = null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="artist_id")
    open var artist:Artist?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="band_id")
    open var band:Band?=null

    @Column(name="role",length=50)
    open var role:String?=null

    @Column(name="joined_year",columnDefinition="YEAR")
    open var joinedYear:Int?=null

    @Column(name="left_year",columnDefinition="YEAR")
    open var leftYear:Int?=null

    @Column(name="nickname",length=50)
    open var nickname:String?=null
}