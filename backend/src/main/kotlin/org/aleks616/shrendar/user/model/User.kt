package org.aleks616.shrendar.user.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name="user",schema="Shrendar")
open class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="user_id",nullable=false)
    open var id:Int?=null

    @Column(name="login",nullable=false,length=50,unique=true)
    open var login:String?=null

    @Column(name="username",nullable=false,length=50)
    open var username:String?=null

    @Column(name="password_hash",nullable=false)
    open var passwordHash:String?=null

    @Column(name="email",nullable=false,unique=true)
    open var email:String?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="rank_id")
    open var rank:Rank?=null

    @Column(name="birth_date")
    open var birthDate:LocalDate?=null

    @Column(name="xp")
    open var xp:Int?=null

    @Column(name="verified")
    open var verified:Boolean?=false
}