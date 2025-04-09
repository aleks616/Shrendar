package org.aleks616.shrendar.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name="events",schema="Shrendar")
open class Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int?=null

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="band_id")
    open var band:Bands?=null

    @Column(name="date")
    open var date:LocalDate?=null

    @Column(name="name",length=30)
    open var name:String?=null

    @Lob
    @Column(name="description", columnDefinition="TEXT")
    open var description:String?=null
}