import React from "react"
import {renderToStaticMarkup} from "react-dom/server"
import {MemoryRouter} from "react-router-dom"
import react from "@vitejs/plugin-react"
import {createServer} from "vite"
import {fileURLToPath} from "node:url"

const webAppRoot=fileURLToPath(new URL("..",import.meta.url))
const storage=new Map()

export function resetLocalStorage(entries={}){
    storage.clear()
    for(const [key,value] of Object.entries(entries)){
        storage.set(key,String(value))
    }
}

globalThis.localStorage={
    getItem(key){
        return storage.has(key)?storage.get(key):null
    },
    setItem(key,value){
        storage.set(key,String(value))
    },
    removeItem(key){
        storage.delete(key)
    },
    clear(){
        storage.clear()
    },
}

const defaultStrings={
    logout: "Log out",
    create_account: "Create account",
    sign_up_to_continue: "Sign Up to Continue",
    email_address: "E-mail address",
    email: "E-mail address",
    login: "Login",
    password: "Password",
    re_enter_password: "Repeat password",
    sign_up: "Sign Up",
    or: "or",
    sign_in: "Sign in",
    login_email: "Login or e-mail address",
    forgot_password_question: "Forgot password?",
    reset_password: "Reset password",
    create_new_password: "Create new password",
    change_password: "Change password",
    something_wrong: "Something went wrong. Try again later.",
    profile: "Profile",
    settings: "Settings",
    username: "Username",
    username_invalid: "Username length must be between 4 and 25 characters",
    birthdate: "Birthdate",
    invalid_user_birthdate: "User must be between 13 and 120 years old",
    save_changes: "Save changes",
    delete_account: "Delete account",
    favorite: "Favorite",
    band: "Band",
    release_date: "Release date",
    anniversary_in: "anniversary in",
    days: "days",
    years_since: "Years since",
    country: "Country",
    age: "Age",
    birthday: "Birthday",
    next_in: "next in",
    death_anniversary: "Death anniversary",
    gender: "Gender",
    zodiac_sign: "Zodiac sign",
    chinese_zodiac_sign: "Chinese zodiac sign",
    bands: "Bands",
    role: "Role",
    no_bands: "No bands",
    status: "Status",
    years_active: "Years active",
    top_genres: "Top genres",
    band_genre_info: "Genres are computed from the band's catalog.",
    present: "present",
    members: "Members",
    albums: "Albums",
    similar_bands: "Similar bands",
    all_members: "All members",
    current_members: "Current members",
    past_members: "Past members",
    favorite_all: "Favorite all",
    person_name: "Person name",
    no_band_members: "No band members",
    title: "Title",
    main_genre: "Main genre",
    no_albums: "No albums",
    thing_name: "Name",
    formed_year: "Formed year",
    no_similar_bands: "No similar bands",
    date: "Date",
    bio: "Bio",
    member_since: "Member since",
    level: "Level",
    rank1: "Rookie",
    rank2: "Apprentice",
    today: "today",
    time_ago: "ago",
}

const testMocks={
    name: "shrendar-component-test-mocks",
    enforce: "pre",
    resolveId(source){
        if(source==="sharedLogic") return "\0shrendar-test-shared-logic"
        if(source==="sharedLogic/localization/comexampleclient_stringsJson.json"){
            return "\0shrendar-test-english-strings"
        }
        if(source==="sharedLogic/localization/comexampleclient_stringsJson_pl.json"){
            return "\0shrendar-test-polish-strings"
        }
        if(source.endsWith("getLanguage.ts")) return "\0shrendar-test-language"
    },
    load(id){
        if(id==="\0shrendar-test-shared-logic"){
            return `
        class EmptyDto {}
        export class Date extends EmptyDto { constructor(year,month,day) { super(); this.year = year; this.month = month; this.day = day; } }
        export class UserDto extends EmptyDto {}
        export class UserProfileDto extends EmptyDto {}
        export class ArtistWikiDto extends EmptyDto { constructor() { super(); this.bands = []; } }
        export class BandWikiDto extends EmptyDto { constructor() { super(); this.bandMembers = []; this.albums = []; this.computedGenres = []; this.similar = []; } }
        export class AlbumWikiDto extends EmptyDto {}
        export class EventWikiDto extends EmptyDto {}
        export class RegisterValidator {
          async validateLogin() { return null }
          async validateEmail() { return null }
          isPasswordValid() { return true }
        }
        export class RegisterRequestDto {}
        export class LoginRequestDto { constructor(login,email,password) { this.login = login; this.email = email; this.password = password } }
        export class ResetPasswordDto {}
        export class TranslationRequestDto { constructor(text,targetLanguage) { this.text = text; this.targetLanguage = targetLanguage } }
        export const RegisterClient = {
          getInstance: () => ({
            register: async () => "verification_code_sent",
            registerConfirm: async () => "account_created"
          })
        }
        export const AccountClient = {
          getInstance: () => ({
            login: async () => JSON.stringify({ token: "test-token" }),
            logout: async () => "logged_out",
            getUserData: async () => null,
            requestPasswordReset: async () => "password_link_sent",
            resetPassword: async () => "password_changed",
            authWithGoogle: async () => JSON.stringify({ token: "google-token" }),
            updateUsername: async () => "username_changed",
            updateEmail: async () => "email_changed",
            addBirthday: async () => "birthday_added",
            deleteAccount: async () => "confirmed"
          })
        }
        export const ProfileClient = {
          getInstance: () => ({
            getUserProfile: async () => null,
            updateBio: async () => "bio_added",
            toggleFavoriteBand: async () => "band_toggled",
            toggleFavoriteArtist: async () => "artist_toggled",
            toggleFavoriteGenre: async () => "genre_toggled"
          })
        }
        export const ArtistClient = {
          getInstance: () => ({
            getArtistWikiPageDataById: async () => null
          })
        }
        export const BandClient = {
          getInstance: () => ({
            getBandWikiPageDataById: async () => null
          })
        }
        export const AlbumClient = {
          getInstance: () => ({
            getAlbumWikiPageData: async () => null
          })
        }
        export const EventClient = {
          getInstance: () => ({
            getEventData: async () => null
          })
        }
        export const LlmClient = {
          getInstance: () => ({
            translate: async () => "Translated description"
          })
        }
      `
        }

        if(id==="\0shrendar-test-language"){
            return 'export function getLanguage() { return "EN" }'
        }

        if(id==="\0shrendar-test-english-strings"){
            return `export default ${JSON.stringify({
                ...defaultStrings,
                special_sign_in_later: "You can sign in later",
                password_link_sent: "Password reset link has been sent to your email",
            })}`
        }

        if(id==="\0shrendar-test-polish-strings"){
            return "export default {}"
        }
    }
}

export async function createComponentTestServer(){
    return createServer({
        configFile: false,
        root: webAppRoot,
        plugins: [react(),testMocks],
        server: {middlewareMode: true},
        appType: "custom",
        logLevel: "error",
    })
}

export async function renderComponent(server,modulePath,exportName,props={}){
    const componentModule=await server.ssrLoadModule(modulePath)
    const componentProps=props.strings===undefined
        ? {...props,strings: defaultStrings}
        : props
    return renderToStaticMarkup(
        React.createElement(
            MemoryRouter,
            null,
            React.createElement(componentModule[exportName],componentProps),
        ),
    )
}
