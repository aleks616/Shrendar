package org.aleks616.shrendar

interface Platform {
    val name:String
}

expect fun getPlatform():Platform