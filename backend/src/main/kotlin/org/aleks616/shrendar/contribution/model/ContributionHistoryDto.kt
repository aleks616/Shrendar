package org.aleks616.shrendar.contribution.model

import java.io.Serializable

data class ContributionHistoryDto(
    val table:String?=null,
    val contributions:List<ContributionDto>?=null
):Serializable