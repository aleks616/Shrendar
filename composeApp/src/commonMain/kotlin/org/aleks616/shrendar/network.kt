package org.aleks616.shrendar

import kotlinx.serialization.Serializable

@Serializable
data class Ranks(val id:Int?,val name:String?,val minXp:Int?)

expect class NetworkClient() {
    suspend fun fetchRanks():List<Ranks>
}
