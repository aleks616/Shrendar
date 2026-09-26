package org.aleks616.shrendar.user.service

object ForbiddenLogins {
    private val forbiddenLogins=setOf(
        "admin",
        "administrator",
        "root",
        "system",
        "guest",
        "test",
        "user",
        "null",
        "undefined",
        "superuser",
        "owner",
        "manager",
        "moderator",
        "support",
        "helpdesk",
        "webmaster",
        "info",
        "contact",
        "sales",
        "marketing",
        "anonymousUser"
    )

    private val forbiddenChars=setOf(' ',',','.','!','?','@','#','$','%','^','&','*','(',')','_','+','=','{','}','[',']','|','\\','/','<','>','"',';',':')

    fun isForbidden(login:String):Boolean {
        return forbiddenLogins.contains(login.lowercase())||forbiddenChars.any{login.contains(it)}
    }
}