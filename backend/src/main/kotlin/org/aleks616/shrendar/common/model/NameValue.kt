package org.aleks616.shrendar.common.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.user.model.UserLog}
 */
data class NameValue(
    val name:String?=null,
    val value:Long?=null
):Serializable