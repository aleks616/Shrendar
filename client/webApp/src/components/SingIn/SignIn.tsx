import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {useState} from "react"
import {Button,ErrorMessage,Form,Heading,Input,Label,TextField} from "@heroui/react"
import {AccountClient,LoginRequestDto} from "sharedLogic"
export function SignIn(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key:string)=>strings[key]??key
    const [login,setLogin]=useState("")
    const [password,setPassword]=useState("")
    const [errorKey,setErrorKey]=useState<string|null>(null)

    const signIn=async ()=>{
        try{
            const isEmail=login.includes("@")
            const loginRequest=new LoginRequestDto((isEmail?null:login),(isEmail?login:null),password)
            const result=await AccountClient.getInstance().login(loginRequest)
            try{
                const token=JSON.parse(result).token
                localStorage.setItem("token",token)
                setErrorKey(null)
            }
            catch(e){
                setErrorKey(result)
            }
        }
        catch(e){
            console.log(e)
            setErrorKey("something_wrong")
        }
    }

    return (
        <div>
            <Heading level={2}>{translate("sign_in")}</Heading>
            <Form className={"flex w-96 flex-col gap-4"}>
                <TextField isRequired name="login" value={login} onChange={setLogin}>
                    <Label>{translate("login_email")}</Label>
                    <Input autoComplete={"email"}/>
                </TextField>
                <TextField isRequired name="password" type="password" value={password} onChange={setPassword}>
                    <Label>{translate("password")}</Label>
                    <Input autoComplete={"current-password"}/>
                </TextField>
                {errorKey&&<ErrorMessage>{translate(errorKey)}</ErrorMessage>}
                <Button
                isDisabled={login.length==0||password.length===0}
                onPress={signIn}>
                    {translate("sign_in")}
                </Button>
            </Form>
        </div>
    )

}
