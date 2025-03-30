package org.aleks616.shrendar

class Greeting {
    private val platform=getPlatform()

    fun greet():String {
        return "Hello, ${platform.name}!"
    }
}