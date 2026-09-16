package com.example.client

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class LocalText {
    fun getGreeting():StringDesc {
        return MR.strings.greeting.desc()
    }

    fun getString(resourceKey: String): StringDesc {
        return when (resourceKey) {
            "greeting" -> MR.strings.greeting.desc()
            else -> throw IllegalArgumentException("Unknown string resource: $resourceKey")
        }
    }
}
