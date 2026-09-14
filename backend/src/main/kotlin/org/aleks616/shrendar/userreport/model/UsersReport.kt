package org.aleks616.shrendar.userreport.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.aleks616.shrendar.user.model.User
import java.time.Instant

@Entity
@Table(name="users_reports")
open class UsersReport {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Long=0L

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="reported_user")
    open var reportedUser:User=User()

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="requesting_user")
    open var requestingUser:User=User()

    @Column(name="at")
    open var at:Instant?=null

    @Column(name="resolved")
    open var resolved:Boolean=false

    @Lob
    @Column(name="description",columnDefinition="TEXT")
    open var description:String?=null

}