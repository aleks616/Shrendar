package org.aleks616.shrendar.homepage.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.contribution.model.ContributionDto}
 */
data class RecentlyAddedDto(
    val changeId:Long?=null,
    val userId:Int?=null,
    val changedRecordId:Long?=null,
    val changedTable:String?=null,
    val data:List<RecentlyAddedRow>?=null,
    val changedAt:String?=null,
):Serializable