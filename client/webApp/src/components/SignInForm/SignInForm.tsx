import React,{useState} from "react"
import {Button,ErrorMessage,Form,Heading,Input,Label,TextField} from "@heroui/react"
import {AccountClient,LoginRequestDto} from "sharedLogic"
import {Navigate} from "react-router-dom";
import LabelledDivider from "../LabelledDivider/LabelledDivider.tsx";
import {SignInWithGoogle} from "../SignInWIthGoogle/SignInWithGoogle.tsx";
export function SignInForm({strings}: { strings: Record<string,string> }){
    const translate=(key:string)=>strings[key]??key
    const [login,setLogin]=useState("")
    const [password,setPassword]=useState("")
    const [errorText,setErrorText]=useState<string|null>(null)
    const [redirect,setRedirect]=useState(false)

    const signIn=async ()=>{
        try{
            const isEmail=login.includes("@")
            const loginRequest=new LoginRequestDto((isEmail?null:login),(isEmail?login:null),password)
            const result=await AccountClient.getInstance().login(loginRequest)
            try{
                const token=JSON.parse(result).token
                localStorage.setItem("token",token)
                setErrorText(null)
                setRedirect(true)
            }
            catch(e){
                setErrorText(translate(result))
            }
        }
        catch(e){
            console.log(e)
            setErrorText(translate("something_wrong"))
        }
    }

    if(redirect){
        return <Navigate to={"/"}/>
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
                {errorText&&<ErrorMessage>{errorText}</ErrorMessage>}
                <Button
                isDisabled={login.length===0||password.length===0}
                onPress={signIn}>
                    {translate("sign_in")}
                </Button>
            </Form>
            <LabelledDivider text={translate("or")}/>
            <div className={"flex justify-center"}>
                <SignInWithGoogle/>
            </div>
        </div>
    )

}
