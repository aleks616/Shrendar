import {AppHeader} from "../../components/AppHeader/AppHeader.tsx";
import {RegisterForm} from "../../components/RegisterForm/RegisterForm.tsx";
import {getLanguage} from "../../components/getLanguage.ts";
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'

export function Register(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <RegisterForm strings={strings} />
            </div>
        </>
    )
}