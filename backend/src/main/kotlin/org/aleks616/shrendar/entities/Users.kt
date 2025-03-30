package org.aleks616.shrendar.entities

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name="users",schema="Shrendar")
open class Users {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="user_id",nullable=false)
    open var id:Int?=null

    @Column(name="login",nullable=false,length=50)
    open var login:String?=null

    @Column(name="username",nullable=false,length=50)
    open var username:String?=null

    @Column(name="password_hash",nullable=false)
    open var passwordHash:String?=null

    @Column(name="email",nullable=false)
    open var email:String?=null

    @Column(name="created_at",nullable=false)
    open var createdAt:Instant?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="rank_id")
    open var ranks:Ranks?=null

    @Column(name="birth_date")
    open var birthDate:LocalDate?=null

    @Column(name="xp")
    open var xp:Int?=null
}