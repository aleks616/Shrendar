import React from 'react'
import {Badge,Button,Description,FieldError,Label,SearchField} from "@heroui/react"
import {ThemeSwitcher} from "../ThemeSwitcher/ThemeSwitcher.tsx"
import {BellIcon,UserIcon,HomeIcon} from '@heroicons/react/24/solid'
import {Link} from "react-router-dom"
import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'

export function AppHeader(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key:string)=>strings[key]??key
    //todo get notifications, hide the icon in 1.0
    const notifications=5
    //profile picture
    const isLoggedIn=false

    return (
        <header className={"flex justify-between items-center w-svw px px-5 m-4"}>
            <div className={"relative size-11 rounded-full bg-background-secondary flex items-center justify-center"}>
                <Link to={"/"}>
                    <HomeIcon className={"relative z-10 size-7"}/>
                </Link>
            </div>

            <div>
                <SearchField variant={"secondary"}>
                    <Label/>
                    <SearchField.Group>
                        <SearchField.SearchIcon/>
                        <SearchField.Input placeholder={"Search..."}/>
                        <SearchField.ClearButton/>
                    </SearchField.Group>
                    <Description/>
                    <FieldError/>
                </SearchField>
            </div>

            <div className={"flex items-center gap-3"}>
                <ThemeSwitcher/>
                {isLoggedIn?(
                    <>
                        <Badge.Anchor>
                            <div
                                className={"relative size-11 rounded-full bg-background-secondary flex items-center justify-center"}>
                                <BellIcon className={"size-7"}/>
                                <Badge color="danger" size={"md"}>5</Badge>
                            </div>
                        </Badge.Anchor>
                        <div
                            className={"relative size-11 rounded-full bg-background-secondary flex items-center justify-center"}>
                            <UserIcon className={"relative z-10 size-7"}/>
                        </div>
                    </>
                ):(
                    <>
                        <Link to={"/register"}>
                            <Button>{translate("sign_up")}</Button>
                        </Link>
                        <Link to={"/login"}>
                            <Button variant={"secondary"}>{translate("sign_in")}</Button>
                        </Link>
                    </>
                )}
            </div>
        </header>
    )
}