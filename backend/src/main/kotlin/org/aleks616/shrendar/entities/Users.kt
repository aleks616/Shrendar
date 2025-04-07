package org.aleks616.shrendar.entities

import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name="users",schema="Shrendar")
open class Users {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="user_id",nullable=false)
    open var id:Int?=null

    @NaturalId
    @Column(name="login",nullable=false,length=50)
    open var login:String?=null

    @Column(name="username",nullable=false,length=50)
    open var username:String?=null

    @Column(name="password_hash",nullable=false)
    open var passwordHash:String?=null

    @NaturalId
    @Column(name="email",nullable=false)
    open var email:String?=null

    @Column(name="created_at",nullable=false)
    open var createdAt:Instant?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="rank_id")
    open var ranks:Ranks?=null

    @Column(name="birth_date")
    open var birthDate:LocalDate?=null

    @Column(name="xp")
    open var xp:Int?=null

    @Column(name="verified")
    open var verified:Boolean?=false
}