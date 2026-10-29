import Cookies from "universal-cookie"

export const supportedLanguages=[
    "EN","PL"
]

export const cookies=new Cookies()

export function getLanguage(){
    if(!Boolean(cookies.get('language'))){
        const codeFromBrowser=navigator.language.substring(0,2).toUpperCase()
        if(supportedLanguages.some(lang => lang===codeFromBrowser))
            cookies.set('language',codeFromBrowser,{path: "/"})
        else
            cookies.set('language',"EN",{path: "/"})
    }
    return cookies.get("language")
}

export function setLanguage(code:string){
    if(supportedLanguages.some(lang => lang===code.toUpperCase())){
        cookies.set('language',code.toUpperCase(),{path: "/"})
        window.location.reload()
    }
    else if(!Boolean(cookies.get('language'))){
        cookies.set('language',"EN",{path: "/"})
        window.location.reload()
    }
    else
        console.log(code+" not supported")

}
