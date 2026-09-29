import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {Button,ErrorMessage} from "@heroui/react"
import {AccountClient} from "sharedLogic"
import {useState} from "react";

export default function Logout(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const [errorText,setErrorText]=useState<string | null>(null)
    const translate=(key: string) => strings[key]??key

    const logout=async () => {
        try{
            const token=localStorage.getItem("token")
            if(token){
                const result=await AccountClient.getInstance().logout(token)
                if(result=="logged_out"){
                    localStorage.removeItem("token")
                    window.location.reload()
                }
                else{
                    setErrorText(translate(result))
                }
            }
            else{
                setErrorText(translate("no_token"))
            }
        }catch(e){
            console.log(e)
        }
    }

    return (
        <div>
            <Button onPress={logout}>
                {translate("logout")}
            </Button>
            {errorText&&<ErrorMessage>{errorText}</ErrorMessage>}
        </div>
    )
}
