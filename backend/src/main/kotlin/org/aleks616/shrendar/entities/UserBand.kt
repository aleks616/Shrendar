package org.aleks616.shrendar.entities

import jakarta.persistence.*

@Entity
@Table(name="user_bands",schema="Shrendar")
open class UserBand {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    open var users:Users?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="band_id")
    open var bands:Bands?=null
}