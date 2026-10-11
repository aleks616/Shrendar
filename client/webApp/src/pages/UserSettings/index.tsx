import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import {Settings} from "../../components/Settings/Settings.tsx"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {getLanguage} from "../../components/getLanguage.ts";
export function UserSettings(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <Settings strings={strings}/>
            </div>
        </>
    )
}