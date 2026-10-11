import React from 'react'
import {CreatePassword} from "../../components/CreatePassword/CreatePassword.tsx"
import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"

export function SetPassword(){

    return(
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <CreatePassword />
            </div>
        </>
    )
}