package org.aleks616.shrendar.userban.model

import jakarta.persistence.*
import org.aleks616.shrendar.user.model.User
import java.time.Instant

@Entity
@Table(name="users_bans")
open class UsersBan {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id",nullable=false)
    open var id:Int=0

    @ManyToOne(fetch=FetchType.EAGER,optional=false)
    @JoinColumn(name="user",nullable=false)
    open var user:User=User()

    @Column(name="at",nullable=false,columnDefinition="DATETIME")
    open var at:Instant=Instant.now()

    @Column(name="until",nullable=false,columnDefinition="DATETIME")
    open var until:Instant=Instant.now()

    @Lob
    @Column(name="description",columnDefinition="TEXT")
    open var description:String?=null

    @ManyToOne(fetch=FetchType.EAGER,optional=false)
    @JoinColumn(name="banned_by")
    open var by:User=User()

    @Column(name="appealed")
    open var appealed:Boolean=false

    @Column(name="appeal_reason")
    open var appealReason:String?=null

    @ManyToOne(fetch=FetchType.EAGER,optional=false)
    @JoinColumn(name="appealed_by")
    open var appealedBy:User=User()
}