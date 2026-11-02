import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import {getLanguage} from "../../components/Language.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {AddArtistForm} from "../../components/AddArtistForm/AddArtistForm.tsx"

export function AddArtist(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    return (
        <>
            <AppHeader/>
            <div className={"flex justify-center w-svw"}>
                <AddArtistForm strings={strings}/>
            </div>
        </>
    )
}