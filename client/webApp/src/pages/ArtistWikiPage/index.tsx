import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import {getLanguage} from "../../components/getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {ArtistData} from "../../components/ArtistData/ArtistData.tsx"
export function ArtistWikiPage(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <ArtistData strings={strings}/>
            </div>
        </>
    )
}