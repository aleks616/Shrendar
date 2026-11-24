import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import {getLanguage} from "../../components/Language.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import spanishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_es.json'
import portugueseStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pt.json'
import {BandData} from "../../components/BandData/BandData.tsx"

export function BandWikiPage(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:lang==="ES"?spanishStrings:lang==="PT"?portugueseStrings:englishStrings
    return (
        <>
            <AppHeader/>
           <div className={"flex justify-center w-svw"}>
               <BandData strings={strings}/>
           </div>
        </>
    )
}