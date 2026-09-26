package com.example.client

import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalTextTest {
    private val localText=LocalText()

    @Test
    fun resolvesACommonStringResource() {
        assertEquals(MR.strings.greeting,localText.getStringResource("greeting"))
    }

    @Test
    fun resolvesAValidationStringResource() {
        assertEquals(
            MR.strings.login_too_short,
            localText.getStringResource("login_too_short")
        )
    }

    @Test
    fun resolvesAResourceWhoseKeyDiffersFromTheResourceName() {
        assertEquals(
            MR.strings.already_have_an_account,
            localText.getStringResource("already_have_account")
        )
    }

    @Test
    fun resolvesAResourceFromAnotherFeature() {
        assertEquals(
            MR.strings.album_addition_received,
            localText.getStringResource("album_addition_received")
        )
    }

    @Test
    fun resolvesTheNotFoundResource() {
        assertEquals(MR.strings.not_found,localText.getStringResource("not_found"))
    }

    @Test
    fun resolvesEverySupportedKey() {
        val supportedKeys=listOf(
            "greeting",
            "login_too_short",
            "login_too_long",
            "login_already_exists",
            "invalid_email",
            "email_already_exists",
            "create_account",
            "sign_up_to_continue",
            "email_address",
            "login",
            "password",
            "re_enter_password",
            "sign_up",
            "or",
            "authorization_successful",
            "authorization_failed",
            "already_have_account",
            "sign_in",
            "passwords_dont_match",
            "invalid_password",
            "special_sign_in_later",
            "too_many_email_requests",
            "too_many_ip_requests",
            "too_many_user_requests",
            "unexpected_error",
            "something_wrong",
            "you_are_banned",
            "invalid_date",
            "band_not_exist",
            "invalid_year",
            "missing_album_add_data",
            "album_exists_already",
            "album_addition_received",
            "missing_album_edit_data",
            "album_id_not_exist",
            "album_title_exists",
            "album_edition_received",
            "album_deletion_received",
            "wrong_importance_studio",
            "wrong_importance_ep",
            "wrong_importance_other",
            "wrong_release_date",
            "genre_not_exist",
            "url_too_long",
            "name_at_least_3",
            "name_at_least_2",
            "invalid_month_day",
            "missing_artist_add_data",
            "artist_addition_received",
            "missing_artist_edit_data",
            "artist_not_exist",
            "artist_edition_received",
            "artist_deletion_received",
            "artist_toggled",
            "invalid_artist_birthdate",
            "artist_too_young",
            "invalid_death_date",
            "invalid_gender",
            "country_not_exist",
            "start_end_not_null",
            "start_before_end",
            "invalid_start_year",
            "invalid_end_year",
            "invalid_status",
            "missing_band_add_data",
            "band_addition_received",
            "band_id_required",
            "missing_band_edit_data",
            "band_edition_received",
            "band_deletion_received",
            "missing_member_add_data",
            "member_exists",
            "member_addition_received",
            "member_id_required",
            "missing_member_edit_data",
            "member_not_exist",
            "member_edition_received",
            "member_deletion_received",
            "band_toggled",
            "formed_year_future",
            "missing_formed",
            "formed_before_min",
            "disbanded_before_min",
            "disbanded_before_formed",
            "inconsistent_status",
            "missing_disbanded",
            "joined_future",
            "left_future",
            "left_before_joined",
            "artist_too_young_joining",
            "artist_dead_joining",
            "artist_leave_when_dead",
            "nickname_too_long",
            "role_too_long",
            "user_not_exist",
            "table_not_exist",
            "confirmation_success",
            "addition_reverted",
            "start_after_end",
            "event_addition_received",
            "event_edition_received",
            "event_delete_received",
            "event_not_exist",
            "event_id_required",
            "missing_event_add_data",
            "event_name_too_long",
            "invalid_event_date",
            "genre_toggled",
            "mod_not_exist",
            "cant_view",
            "page_not_found",
            "user_banned",
            "appeal_submitted",
            "ban_cancelled",
            "rank_too_low",
            "no_report_reason",
            "report_success",
            "resolved_success",
            "verification_code_sent",
            "bio_added",
            "confirmed",
            "birthday_added",
            "user_too_young",
            "account_not_found",
            "email_change",
            "new_email_exists",
            "usernamed_changed",
            "usernamed_change_limit",
            "username_taken",
            "logged_out",
            "no_token",
            "account_created",
            "invalid_credentials",
            "password_changed",
            "resend_code_in",
            "resend_code",
            "confirm_account",
            "done",
            "invalid_code",
            "no_code",
            "login_email",
            "welcome",
            "sign_in_up",
            "account_already",
            "continue_as",
            "guest_question",
            "back",
            "logout"
        )

        supportedKeys.forEach {key->
            localText.getStringResource(key)
        }
    }

    @Test
    fun createsAResourceStringDescription() {
        assertEquals(MR.strings.greeting.desc(),localText.getStringDesc("greeting"))
    }

    @Test
    fun rejectsUnknownKeys() {
        assertFailsWith<IllegalArgumentException> {
            localText.getStringResource("does_not_exist")
        }
    }
}
