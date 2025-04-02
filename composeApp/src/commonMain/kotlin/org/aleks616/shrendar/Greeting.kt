package org.aleks616.shrendar

class Greeting {
    private val platform=getPlatformFull()
    private val platformShort=getPlatformShort()

    fun greet():String {
        return "Hello, ${platform.name}!"
    }
    fun getPlatform():String{
        return platformShort.name
    }
}