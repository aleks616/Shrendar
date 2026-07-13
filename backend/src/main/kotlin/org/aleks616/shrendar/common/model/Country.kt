package org.aleks616.shrendar.common.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="countries",schema="Shrendar")
open class Country {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @Column(name="name",length=60)
    open var name:String?=null
}