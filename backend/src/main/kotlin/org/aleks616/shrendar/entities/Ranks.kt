package org.aleks616.shrendar.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="ranks",schema="Shrendar")
open class Ranks {
    @Id
    @Column(name="rank_id",nullable=false)
    open var id:Int?=null

    @Column(name="name",length=30)
    open var name:String?=null

    @Column(name="min_xp",nullable=false)
    open var minXp:Int?=null
}