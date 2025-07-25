package org.aleks616.shrendar.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name="artist",schema="Shrendar")
open class Artists {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="artist_id",nullable=false)
    open var id:Int?=null

    @Column(name="name",nullable=false,length=200)
    open var name:String?=null

    @Column(name="birthdate")
    open var birthDate:LocalDate?=null

    @Column(name="death_date")
    open var deathDate:LocalDate?=null

    @Column(name="gender")
    open var gender:Char?=null

    @Column(name="description")
    open var description:String?=null

    @Column(name="country")
    open var country:Int?=null
}