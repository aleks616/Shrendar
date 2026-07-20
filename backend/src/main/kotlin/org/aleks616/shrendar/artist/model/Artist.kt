package org.aleks616.shrendar.artist.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name="artist",schema="Shrendar")
open class Artist {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="artist_id",nullable=false)
    open var id:Int?=null

    @Column(name="name",nullable=false,length=200)
    open var name:String?=null

    @Column(name="birth_date")
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