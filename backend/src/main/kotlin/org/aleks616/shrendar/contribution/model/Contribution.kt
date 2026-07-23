package org.aleks616.shrendar.contribution.model

import jakarta.persistence.*
import org.aleks616.shrendar.user.model.User
import java.time.LocalDateTime

@Entity
@Table(name="contribution",schema="Shrendar")
open class Contribution {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @Column(name="change_id")
    open var changeId:Int?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="user_id")
    open var user:User?=null

    @Enumerated(EnumType.STRING)
    @Column(name="action",columnDefinition="ENUM('create', 'update', 'delete')")
    open var action:Action?=null

    @Column(name="changed_table",length=30)
    open var changedTable:String?=null

    @Column(name="changed_column",length=40)
    open var changedColumn:String?=null

    @Column(name="changed_record_id")
    open var changedRecordId:Int?=null

    @Column(name="old_value",length=50)
    open var oldValue:String?=null

    @Column(name="new_value",length=50)
    open var newValue:String?=null

    @Column(name="changed_at")
    open var changedAt:LocalDateTime?=null

    @Column(name="confirmed")
    open var confirmed:Boolean?=null

    @Column(name="confirmed_by")
    open var confirmedBy:Int?=null
}


enum class Action{
    create,
    update,
    delete
}


