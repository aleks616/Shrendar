package org.aleks616.shrendar.user.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name="user_logs")
open class UserLog {
    @Id
    @Column(name="user_id",nullable=false)
    open var id:Int=0

    @MapsId
    @OneToOne(fetch=FetchType.EAGER,optional=false)
    @JoinColumn(name="user_id",nullable=false)
    open var user:User?=null

    @Column(name="account_created_time")
    open var accountCreatedTime:Instant?=null

    @Column(name="last_login_time")
    open var lastLoginTime:Instant?=null

    @Column(name="account_deletion_scheduled_time")
    open var accountDeletionScheduledTime:Instant?=null

    @Column(name="display_name_changed_time")
    open var displayNameChangedTime:Instant?=null

    @Column(name="birthday_changed_time")
    open var birthdayChangedTime:Instant?=null

    @Column(name="password_change_time")
    open var passwordChangedTime:Instant?=null

}