package com.example.client

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class LocalText {
    fun getString(resourceKey:String):StringDesc {
        return when(resourceKey) {
            "greeting"->MR.strings.greeting.desc()
            "login_too_short"->MR.strings.login_too_short.desc()
            "login_too_long"->MR.strings.login_too_long.desc()
            "login_already_exists"->MR.strings.login_already_exists.desc()
            "invalid_email"->MR.strings.invalid_email.desc()
            "email_already_exists"->MR.strings.email_already_exists.desc()
            "create_account"->MR.strings.create_account.desc()
            "sign_up_to_continue"->MR.strings.sign_up_to_continue.desc()
            "email_address"->MR.strings.email_address.desc()
            "login"->MR.strings.login.desc()
            "password"->MR.strings.password.desc()
            "re_enter_password"->MR.strings.re_enter_password.desc()
            "sign_up"->MR.strings.sign_up.desc()
            "or"->MR.strings.or.desc()
            "authorization_successful"->MR.strings.authorization_successful.desc()
            "authorization_failed"->MR.strings.authorization_failed.desc()
            "already_have_account"->MR.strings.already_have_an_account.desc()
            "sign_in"->MR.strings.sign_in.desc()
            "passwords_dont_match"->MR.strings.passwords_dont_match.desc()
            "invalid_password"->MR.strings.invalid_password.desc()
            "special_sign_in_later"->MR.strings.special_sign_in_later.desc()
            else->throw IllegalArgumentException("Unknown string resource: $resourceKey")
        }
    }
}
