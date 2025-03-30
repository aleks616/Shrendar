package org.aleks616.shrendar.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name="contributions",schema="Shrendar")
open class Contributions {
    @Id
    @Column(name="contribution_id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    open var users:Users?=null


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
    open var changedAt:Instant?=null
}


enum class Action{
    create,
    update,
    delete
}


