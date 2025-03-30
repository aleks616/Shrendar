package org.aleks616.shrendar.entities

import jakarta.persistence.*

@Entity
@Table(name="bands",schema="Shrendar")
open class Bands {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="band_id",nullable=false)
    open var id:Int?=null

    @Column(name="name",nullable=false,length=80)
    open var name:String?=null

    @Column(name="formed_year",columnDefinition="YEAR")
    open var formedYear:Int?=null

    @Column(name="disbanded_year",columnDefinition="YEAR")
    open var disbandedYear:Int?=null
}