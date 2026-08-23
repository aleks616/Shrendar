package org.aleks616.shrendar.contribution.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.contribution.model.Contribution}
 */
data class ContributionDto(
    val id:Int?=null,
    val changeId:Int?=null,
    val userId:Int?=null,
    val action:Action?=null,
    val changedTable:String?=null,
    val changedColumn:String?=null,
    val changedRecordId:Int?=null,
    val oldValue:String?=null,
    val newValue:String?=null,
    val changedAt:String?=null,
    val confirmed:Boolean?=null,
    val confirmedBy:Int?=null
):Serializable