package org.aleks616.shrendar.user.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="`rank`",schema="Shrendar")
open class Rank {
    @Id
    @Column(name="rank_id",nullable=false)
    open var id:Int=0

    @Column(name="name",length=35)
    open var name:String?=null

    @Column(name="min_xp",nullable=false)
    open var minXp:Int=0

    @Column(name="allowed_contributions")
    open var allowedContributions:Int?=null
}