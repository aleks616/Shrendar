import Cookies from "universal-cookie"
import {LocalText} from "sharedLogic";
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
export const supportedLanguages=[
    "EN","PL"
]

export const cookies=new Cookies()
export function getLanguage(){
    if(!Boolean(cookies.get('language'))){
        const codeFromBrowser=navigator.language.substring(0,2).toUpperCase()
        if(supportedLanguages.some(lang => lang === codeFromBrowser))
            cookies.set('language',codeFromBrowser,{path:"/"})
        else
            cookies.set('language',"EN",{path:"/"})
    }
    return cookies.get("language")
}

const localText=new LocalText()
export function localize(key:string,lang:string="EN"):string{
    const strings=lang==="PL"?polishStrings:englishStrings
    const stringDesc=localText.getStringDesc(key)
    // @ts-ignore
    const localizedString=strings[stringDesc.stringRes_1.key_1]
    if(localizedString)
        return localizedString
    console.log(`Missing localization for key: ${key} in language: ${lang}`)
    return "unexpected_error"
}
