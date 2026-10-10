import {getLanguage} from "../../components/getLanguage.ts"
import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import {EventData} from "../../components/EventData/EventData.tsx"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'

export function EventWikiPage(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <EventData strings={strings}/>
            </div>
        </>
    )
}