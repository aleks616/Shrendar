import {AppHeader} from "../../components/AppHeader/AppHeader.tsx";
import {SignInForm} from "../../components/SignInForm/SignInForm.tsx";
import {getLanguage} from "../../components/getLanguage.ts";
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'

export function Login(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw h-screen"}>
                <SignInForm strings={strings} />
            </div>
        </>
    )
}