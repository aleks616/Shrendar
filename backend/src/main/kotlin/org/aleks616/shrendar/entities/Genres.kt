package org.aleks616.shrendar.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="genres",schema="Shrendar")
open class Genres {
    @Id
    @Column(name="genre_id",nullable=false)
    open var id:Int?=null

    @Column(name="name",nullable=false,length=80)
    open var name:String?=null

    @Column(name="properties",length=7)
    open var properties:String?="0000000"
}