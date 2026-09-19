package com.example.client

import kotlin.js.JsExport

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
class Greeting {
    private val platform=getPlatform()

    fun greet():String {
        return sayHello(platform.name)
    }
}