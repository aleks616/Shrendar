import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {Button} from "@heroui/react"
import {AccountClient} from "sharedLogic"

export default function Logout(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
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
                    console.log(translate(result))
                }
            }
            else{
                console.log("no token")
            }
        }catch(e){
            console.log(e)
        }
    }

    return (
        <Button onPress={logout}>
            {translate("logout")}
        </Button>
    )
}
