package org.aleks616.shrendar.genre.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="genre",schema="Shrendar")
open class Genre {
    @Id
    @Column(name="genre_id",nullable=false)
    open var id:Int=0

    @Column(name="name",nullable=false,length=80)
    open var name:String=""

    @Column(name="properties",length=7)
    open var properties:String?="0000000"
}