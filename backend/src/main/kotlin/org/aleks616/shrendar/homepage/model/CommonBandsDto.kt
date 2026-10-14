package org.aleks616.shrendar.homepage.model

import org.aleks616.shrendar.band.model.ArtistBandsDto
import java.io.Serializable

data class CommonBandsDto(
    val bands:List<ArtistBandsDto>?=null
):Serializable