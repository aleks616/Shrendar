package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersBands
import org.springframework.data.jpa.repository.JpaRepository

interface UserBandRepository:JpaRepository<UsersBands,Long> {
    fun findByBandAndUser(band:Band,user:User):UsersBands?
    fun findByUser(user:User):MutableList<UsersBands>
    fun findByBandIdAndUser_Login(bandId:Int,userLogin:String):MutableList<UsersBands>
}