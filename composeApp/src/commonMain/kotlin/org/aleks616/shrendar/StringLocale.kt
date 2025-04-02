package org.aleks616.shrendar

object StringLocale{
    const val WELCOME="welcome"

    const val CREATE_LOGIN_TITLE="create_Login_T"
    const val CREATE_LOGIN_DESCRIPTION="create_Login_D"
    const val CREATE_LOGIN_PLACEHOLDER="create_Login_P"

    const val DISPLAY_NAME_TITLE="displayName_T"
    const val DISPLAY_NAME_DESCRIPTION="displayName_D"
    const val DISPLAY_NAME_PLACEHOLDER="displayName_P"

    const val ENTER_EMAIL_TITLE="enterEmail_T"
    const val ENTER_EMAIL_PLACEHOLDER="enterEmail_P"

    const val CREATE_PASSWORD_TITLE="enterPassword_T"
    const val PASSWORD_PLACEHOLDER="password_P"
    const val CONFIRM_PASSWORD_TITLE="confirmPassword_T"

    const val ERROR_PASSWORD_NOT_MATCH="passwordsDontMatch"
    const val ERROR_PASSWORD_NOT_SAFE="passwordNotSafe"
    const val ERROR_LOGIN_EMPTY="loginEmpty"
    const val ERROR_EMAIL_INCORRECT="emailEmpty"
    const val PASSWORD_SAFE_MESSAGE="passwordSafetyGuide"



    const val ENTER_LOGIN_TITLE="enterLogin_T"
    const val ENTER_LOGIN_DESCRIPTION="enterLogin_D"
    const val ENTER_LOGIN_PLACEHOLDER="enterLogin_P"

    val translations=mapOf(
        //region
        CREATE_LOGIN_TITLE to mapOf(
            "en" to "Create a login.",
            "pl" to "Stwórz login."
        ),
        CREATE_LOGIN_DESCRIPTION to mapOf(
            "en" to "It won't be displayed.",
            "pl" to "Nie będzie on widoczny."
        ),
        CREATE_LOGIN_PLACEHOLDER to mapOf(
            "en" to "login",
            "pl" to "login"
        ),

        DISPLAY_NAME_TITLE to mapOf(
            "en" to "display name",
            "pl" to "wyświetlana nazwa użytkownika"
        ),
        DISPLAY_NAME_DESCRIPTION to mapOf(
            "en" to "Create a display name, visible on your profile",
            "pl" to "Nazwę użytkownika która będzie widoczna"
        ),
        DISPLAY_NAME_PLACEHOLDER to mapOf(
            "en" to "display name",
            "pl" to "wyświetlana nazwa użytkownika"
        ),

        ENTER_EMAIL_TITLE to mapOf(
            "en" to "Enter E-mail address",
            "pl" to "Podaj E-mail"
        ),
        ENTER_EMAIL_PLACEHOLDER to mapOf(
            "en" to "E-mail address",
            "pl" to "Adres e-mail"
        ),

        CREATE_PASSWORD_TITLE to mapOf(
            "en" to "Create password",
            "pl" to "Utwórz hasło"
        ),
        PASSWORD_PLACEHOLDER to mapOf(
            "en" to "password",
            "pl" to "hasło"
        ),
        CONFIRM_PASSWORD_TITLE to mapOf(
            "en" to "Confirm password",
            "pl" to "Potwierdź hasło",
        ),
        //endregion
        ERROR_PASSWORD_NOT_MATCH to mapOf(
            "en" to "Passwords don't match",
            "pl" to "Hasła się różnią",
        ),
        ERROR_PASSWORD_NOT_SAFE to mapOf(
            "en" to "Password isn't strong enough",
            "pl" to "Hasło jest za słabe",
        ),
        PASSWORD_SAFE_MESSAGE to mapOf(
            "en" to "Your password must contain: an uppercase letter, a number, a haiku, a gang sign, a hieroglyph, and the blood of a virgin.",
            "pl" to "Twoje hasło musi zawierać: wielką literę, cyfrę, haiku, znak gangu, hieroglif i krew dziewicy.", //todo: change to unfunny
        ),
        ERROR_LOGIN_EMPTY to mapOf(
            "en" to "Login cannot be empty.",
            "pl" to "Login nie może być pusty.",
        ),
        ERROR_EMAIL_INCORRECT to mapOf(
            "en" to "E-mail address isn't valid.",
            "pl" to "Adres e-mail jest niepoprawny.",
        ),


        ENTER_LOGIN_TITLE to mapOf(
            "en" to "Enter login",
            "pl" to "Podaj login"
        ),

        ENTER_LOGIN_DESCRIPTION to mapOf(
            "en" to "It won't be displayed.",
            "pl" to "Nie będzie on widoczny."
        ),
        ENTER_LOGIN_PLACEHOLDER to mapOf(
            "en" to "login",
            "pl" to "login"
        ),

        WELCOME to mapOf(
            "en" to "Welcome",
            "pl" to "Witaj"
        ),

    )


}