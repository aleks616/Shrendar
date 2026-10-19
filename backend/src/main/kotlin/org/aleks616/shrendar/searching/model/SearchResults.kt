package org.aleks616.shrendar.searching.model

import java.io.Serializable

data class SearchResults(
    val items:List<SearchResultItem>?=null
):Serializable
