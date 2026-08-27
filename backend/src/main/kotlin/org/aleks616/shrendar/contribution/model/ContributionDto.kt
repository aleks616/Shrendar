package org.aleks616.shrendar.contribution.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.contribution.model.Contribution}
 */
data class ContributionDto(
    val id:Long?=null,
    val changeId:Long?=null,
    val userId:Int?=null,
    val action:Action?=null,
    val changedTable:String?=null,
    val changedColumn:String?=null,
    val changedRecordId:Long?=null,
    val oldValue:String?=null,
    val newValue:String?=null,
    val changedAt:String?=null,
    val confirmed:Boolean?=null,
    val confirmedBy:Int?=null
):Serializable