import {getLanguage} from "../../components/getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {UserProfile} from "../../components/UserProfile/UserProfile.tsx";
import {AppHeader} from "../../components/AppHeader/AppHeader.tsx";

export function ProfilePage(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <UserProfile strings={strings}/>
            </div>
        </>
    )
}