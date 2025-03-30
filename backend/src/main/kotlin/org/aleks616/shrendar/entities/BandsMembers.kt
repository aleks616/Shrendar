package org.aleks616.shrendar.entities

import jakarta.persistence.*

@Entity
@Table(name="bands_members",schema="Shrendar")
open class BandsMembers {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id: Int? = null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="artist_id")
    open var artist:Artists?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="band_id")
    open var bands:Bands?=null

    @Column(name="role",length=50)
    open var role:String?=null

    @Column(name="joined_year",columnDefinition="YEAR")
    open var joinedYear:Int?=null

    @Column(name="left_year",columnDefinition="YEAR")
    open var leftYear:Int?=null
}