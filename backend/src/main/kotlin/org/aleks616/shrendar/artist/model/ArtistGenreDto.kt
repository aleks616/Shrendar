package org.aleks616.shrendar.artist.model

import org.aleks616.shrendar.common.model.NameValue
import java.io.Serializable

/**
 * DTO for {@link javax.imageio.plugins.tiff.BaselineTIFFTagSet.Artist}
 */
data class ArtistGenreDto(
    val artistId:Long?=null,
    val artistName:String?=null,
    val genres:List<NameValue>?=null
):Serializable