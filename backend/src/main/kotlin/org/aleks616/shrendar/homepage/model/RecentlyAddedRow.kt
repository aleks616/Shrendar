package org.aleks616.shrendar.homepage.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.homepage.model.RecentlyAddedDto}
 */
data class RecentlyAddedRow(
    val id:Long?=null,
    val changedColumn:String?=null,
    val oldValue:String?=null,
    val newValue:String?=null
):Serializable