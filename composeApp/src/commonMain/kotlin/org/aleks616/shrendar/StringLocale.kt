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
    const val ERROR_LOGIN_ALREADY_EXISTS="loginAlreadyExists"
    const val ERROR_EMAIL_ALREADY_EXISTS="emailAlreadyExists"

    const val ERROR_EMAIL_DOES_NOT_EXIST="emailDoesNotExist"
    const val ERROR_LOGIN_DOES_NOT_EXIST="loginDoesNotExist"


    const val CREATE_ACCOUNT="createAccount"

    const val ENTER_PASSWORD_TITLE="enterPassword_T"
    const val ENTER_PASSWORD_PLACEHOLDER="enterPassword_P"

    const val LOG_IN_BUTTON="logInButton"

    const val ENTER_LOGIN_TITLE="enterLogin_T"
    const val ENTER_LOGIN_DESCRIPTION="enterLogin_D"
    const val ENTER_LOGIN_PLACEHOLDER="enterLogin_P"


    const val TODAY="today"
    const val SEARCH="search"

    const val RANK1="Newbie"
    const val RANK2="Kindermetal"
    const val RANK3="CD Borrower"
    const val RANK4="Classics Fan"
    const val RANK5="Metalhead"
    const val RANK6="Showgoer"
    const val RANK7="Blasphemer"
    const val RANK8="Collector"
    const val RANK9="Real fan"
    const val RANK10="Satan's little helper"
    const val RANK11="Pick catcher"
    const val RANK12="Veteran"
    const val RANK13="No.1 Fan"
    const val RANK14="Cowboy from Hell"
    const val RANK15="Hellraiser"
    const val RANK16="Holier than Thou"
    const val RANK17MOD="Right hand of Lucifer (Moderator)"
    const val RANK18WEBMASTER="Price of Darkness (Webmaster)"


    val translations=mapOf(
        //region login/register
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
        ERROR_LOGIN_ALREADY_EXISTS to mapOf(
            "en" to "Account with that login already exists. Choose a different one or log in instead.",
            "pl" to "Ta nazwa użytkownika jest zajęta, wybierz inną lub zaloguj się."
        ),
        ERROR_EMAIL_ALREADY_EXISTS to mapOf(
            "en" to "Account with that e-mail already exists. Log in instead.",
            "pl" to "Konto z podanym adresem e-mail już istnieje, wprowadź inny lub zaloguj się."
        ),
        ERROR_LOGIN_DOES_NOT_EXIST to mapOf(
            "en" to "Account with that login doesn't exists.",
            "pl" to "Konto z taką nazwą użytkownika nie istnieje."
        ),
        ERROR_EMAIL_DOES_NOT_EXIST to mapOf(
            "en" to "Account with that e-mail address doesn't exists.",
            "pl" to "Konto z takim adresem e-mail nie istnieje."
        ),


        CREATE_ACCOUNT to mapOf(
            "en" to "Create account.",
            "pl" to "Utwórz konto."
        ),
        ENTER_PASSWORD_TITLE to mapOf(
            "en" to "Enter password",
            "pl" to "Podaj hasło"
        ),
        ENTER_PASSWORD_PLACEHOLDER to mapOf(
            "en" to "password",
            "pl" to "hasło"
        ),

        LOG_IN_BUTTON to mapOf(
            "en" to "Log in",
            "pl" to "Zaloguj się"
        ),
        ENTER_LOGIN_TITLE to mapOf(
            "en" to "Enter login or e-mail",
            "pl" to "Podaj login lub adres e-mail"
        ),

        ENTER_LOGIN_DESCRIPTION to mapOf(
            "en" to "It won't be displayed.",
            "pl" to "Nie będzie on widoczny."
        ),
        ENTER_LOGIN_PLACEHOLDER to mapOf(
            "en" to "Login or e-mail",
            "pl" to "Login lub e-mail"
        ),
        //endregion

        //region rank
        RANK1 to mapOf(
            "en" to "Newbie",
            "pl" to "Nowicjusz"
        ),
        RANK2 to mapOf(
            "en" to "Kindermetal",
            "pl" to "Kindermetal"
        ),
        RANK3 to mapOf(
            "en" to "CD Borrower",
            "pl" to "CD Borrower"
        ),
        RANK4 to mapOf(
            "en" to "Classics Fan",
            "pl" to "Fan klasyków"
        ),
        RANK5 to mapOf(
            "en" to "Metalhead",
            "pl" to "Metalowiec"
        ),
        RANK6 to mapOf(
            "en" to "Showgoer",
            "pl" to "Uczestnik koncertów"
        ),
        RANK7 to mapOf(
            "en" to "Blasphemer",
            "pl" to "Bluźnierca"
        ),
        RANK8 to mapOf(
            "en" to "Collector",
            "pl" to "Kolekcjoner"
        ),
        RANK9 to mapOf(
            "en" to "Real fan",
            "pl" to "Prawdziwy fan"
        ),
        RANK10 to mapOf(
            "en" to "Satan's little helper",
            "pl" to "Satan's little helper"
        ),
        RANK11 to mapOf(
            "en" to "Pick catcher",
            "pl" to "Pick catcher"
        ),
        RANK12 to mapOf(
            "en" to "Veteran",
            "pl" to "Weteran"
        ),
        RANK13 to mapOf(
            "en" to "No.1 Fan",
            "pl" to "Fan nr. 1"
        ),
        RANK14 to mapOf(
            "en" to "Cowboy from Hell",
            "pl" to "Cowboy from Hell"
        ),
        RANK15 to mapOf(
            "en" to "Hellraiser",
            "pl" to "Hellraiser"
        ),
        RANK16 to mapOf(
            "en" to "Holier than Thou",
            "pl" to "Holier than Thou"
        ),
        RANK17MOD to mapOf(
            "en" to "Right hand of Lucifer (Moderator)",
            "pl" to "Right hand of Lucifer (Moderator)"
        ),
        RANK18WEBMASTER to mapOf(
            "en" to "Price of Darkness (Webmaster)",
            "pl" to "Price of Darkness (Webmaster)"
        ),
        //endregion



        TODAY to mapOf(
            "en" to "Today",
            "pl" to "Dzisiaj"
        ),
        SEARCH to mapOf(
            "en" to "Search",
            "pl" to "Szukaj"
        ),

        WELCOME to mapOf(
            "en" to "Welcome",
            "pl" to "Witaj"
        ),

    )


}