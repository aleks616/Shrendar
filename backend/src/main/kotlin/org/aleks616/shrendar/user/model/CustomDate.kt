package org.aleks616.shrendar.user.model

import java.io.Serializable
import java.time.LocalDate

data class CustomDate(
    val year:Int,
    val month:Int,
    val day:Int
):Serializable

fun LocalDate.toCustomDate():CustomDate {
    return CustomDate(
        year=this.year,
        month=this.monthValue,
        day=this.dayOfMonth
    )
}
