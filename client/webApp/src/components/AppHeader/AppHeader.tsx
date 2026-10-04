import React from 'react'
import {Badge,Button,Description,Dropdown,FieldError,Label,SearchField} from "@heroui/react"
import {ThemeSwitcher} from "../ThemeSwitcher/ThemeSwitcher.tsx"
import {
    BellIcon,UserIcon,HomeIcon,ArrowRightEndOnRectangleIcon as ExitIcon,Cog8ToothIcon as SettingsIcon,
    UserCircleIcon
} from '@heroicons/react/24/solid'
import {Link} from "react-router-dom"
import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {logout} from "../Logout/Logout.tsx"
import {AccountClient} from "sharedLogic";

export function AppHeader(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key: string) => strings[key]??key
    //todo get notifications, hide the icon in 1.0
    const notifications=5
    //profile picture
    const token=localStorage.getItem("token")
    const isLoggedIn=token!==null&&token!==undefined&&token.length>0
    const [login,setLogin]=React.useState<string|null>(localStorage.getItem("login"))

    const getUserData=async () => {
        const token=localStorage.getItem("token")
        if(token!==null&&token!==undefined&&token.length>0){
            const result=await AccountClient.getInstance().getUserData(token)
            if(result.login!==null&&result.login!==undefined){
                localStorage.setItem("login",result.login)
                setLogin(result.login)
            }
        }
    }

    if(login===null){
        getUserData()
    }

    return (
        <header className={"flex justify-between items-center w-svw my-4"}>
            <div className={"size-11 rounded-full bg-background-secondary flex items-center justify-center mx-3"}>
                <Link to={"/"}>
                    <HomeIcon className={"size-7"}/>
                </Link>
            </div>

            {/*<div>
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
            </div>*/}

            <div className={"flex items-center gap-3 mx-3"}>
                <ThemeSwitcher/>
                {isLoggedIn?(
                    <>
                        <Badge.Anchor>
                            <div className={"size-11 rounded-full bg-background-secondary flex items-center justify-center"}>
                                <BellIcon className={"size-7"}/>
                                <Badge color="danger" size={"md"}>5</Badge>
                            </div>
                        </Badge.Anchor>
                        <Dropdown>
                            <Dropdown.Trigger>
                                <div className={"size-11 rounded-full bg-background-secondary flex items-center justify-center"}>
                                    <UserIcon className={"size-7"}/>
                                </div>
                            </Dropdown.Trigger>
                            <Dropdown.Popover>
                                <Dropdown.Menu>
                                    <Dropdown.Item>
                                        <Link to={"/u/"+login} className={"relative flex gap-3 w-full"}>
                                            <UserCircleIcon className={"relative size-7"}/>
                                            <p className={"text-lg"}>Profile</p>
                                        </Link>
                                    </Dropdown.Item>
                                    <Dropdown.Item>
                                        <SettingsIcon className={"size-7"}/>
                                        <p className={"text-lg"}>Settings</p>
                                    </Dropdown.Item>
                                    <Dropdown.Item onPress={logout}>
                                        <ExitIcon className={"size-7"}/>
                                        <p className={"text-lg"}>{translate("logout")}</p>
                                    </Dropdown.Item>
                                </Dropdown.Menu>
                            </Dropdown.Popover>
                        </Dropdown>
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