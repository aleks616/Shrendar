package org.aleks616.shrendar.searching.model

import java.io.Serializable

data class SearchResultItem(
    val name:String?=null,
    val info:String?=null,
    val description:String?=null,
    val link:String?=null,
    val type:String?=null,
    val imageUrl:String?=null
):Serializable
