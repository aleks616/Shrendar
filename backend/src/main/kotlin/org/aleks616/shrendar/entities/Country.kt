package org.aleks616.shrendar.entities

import jakarta.persistence.*

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