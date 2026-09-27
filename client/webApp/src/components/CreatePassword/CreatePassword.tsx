import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {useState} from "react"
import {Button,ErrorMessage,Form,Heading,Input,Label,TextField} from "@heroui/react"
import {AccountClient,RegisterValidator,ResetPasswordDto} from "sharedLogic";
export function CreatePassword() {
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key:string)=>strings[key]??key
    const [password,setPassword]=useState("")
    const [repeatPassword,setRepeatPassword]=useState("")
    const [errorKey,setErrorKey]=useState<string | null>(null)

    const createPassword=()=>{
        try{
            const urlParams=new URLSearchParams(window.location.search)
            const code=urlParams.get("code")
            const email=urlParams.get("account")
            console.log(code,email)
            if(code==null||email==null){
                console.log("invalid parameters") //todo
                return
            }
            if(password!==repeatPassword){
                setErrorKey("passwords_dont_match")
                return
            }
            const validator=new RegisterValidator()
            if(!validator.isPasswordValid(password)){
                setErrorKey("invalid_password")
                return
            }
            const resetPasswordRequest=new ResetPasswordDto(email,password,code,lang)
            const result=AccountClient.getInstance().resetPassword(resetPasswordRequest)
            console.log(result)
            //todo
        }
        catch(e){
            console.log(e)
            setErrorKey("something_wrong")
        }
    }

    return (
        <div>
            <Heading level={2}>Create new password</Heading>
            <Form className="flex w-96 flex-col gap-4">
                <TextField isRequired name="password" type="password" value={password} onChange={setPassword}>
                    <Label>{translate("password")}</Label>
                    <Input autoComplete="new-password"/>
                </TextField>
                <TextField isRequired name="repeatPassword" type="password" value={repeatPassword} onChange={setRepeatPassword}>
                    <Label>{translate("re_enter_password")}</Label>
                    <Input autoComplete="new-password"/>
                </TextField>
                {errorKey&&<ErrorMessage>{translate(errorKey)}</ErrorMessage>}
                <Button onPress={createPassword} isDisabled={password.length===0||repeatPassword.length===0}>
                    Change password
                </Button>
            </Form>
        </div>
    )
}
