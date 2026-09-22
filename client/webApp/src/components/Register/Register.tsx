import './Register.css'
import {getLanguage} from "../getLanguage.ts";
import {Button,Form,Input,Label,TextField,Heading,ErrorMessage,Separator} from '@heroui/react';
import {RegisterClient,RegisterRequest,RegisterValidator} from "sharedLogic";
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json';
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json';
import {useState} from "react";
import type {FormEvent} from "react";

//GenreClient.getInstance().getAll().then((genres)=>console.log(genres))
export function Register(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key: string) => strings[key]??key

    const [email,setEmail]=useState("")
    const [login,setLogin]=useState("")
    const [password,setPassword]=useState("")
    const [repeatPassword,setRepeatPassword]=useState("")
    const [errorKey,setErrorKey]=useState<string | null>(null)

    const onSubmit=async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()

        const validator=new RegisterValidator()
        try{
            const loginError=await new RegisterValidator().validateLogin(login)
            if(loginError){
                setErrorKey(loginError)
                return
            }

            const emailError=await validator.validateEmail(email)
            if(emailError){
                setErrorKey(emailError==="email_invalid"?"invalid_email":emailError)
                return
            }

            if(password!==repeatPassword){
                setErrorKey("passwords_dont_match")
                return
            }

            if(!validator.isPasswordValid(password)){
                setErrorKey("invalid_password")
                return
            }

            setErrorKey(null)
            const registerRequest=new RegisterRequest(login,login,email,password,lang)
            await RegisterClient.getInstance().register(registerRequest)
        }
        catch(e){
            console.log(e)
            setErrorKey("unexpected_error")
        }
    }

    return (
        <div>
            <Heading level={1}>{translate("create_account")}</Heading>
            <Heading level={3}>{translate("sign_up_to_continue")}</Heading>
            <Form className="flex w-96 flex-col gap-4" onSubmit={onSubmit}>
                <TextField isRequired name="email" type="email" value={email} onChange={setEmail}>
                    <Label>{translate("email_address")}</Label>
                    <Input autoComplete="email"/>
                </TextField>
                <TextField isRequired name="login" value={login} onChange={setLogin}>
                    <Label>{translate("login")}</Label>
                    <Input autoComplete="username"/>
                </TextField>
                <TextField isRequired name="password" type="password" value={password} onChange={setPassword}>
                    <Label>{translate("password")}</Label>
                    <Input autoComplete="new-password"/>
                </TextField>
                <TextField isRequired name="repeatPassword" type="password" value={repeatPassword} onChange={setRepeatPassword}>
                    <Label>{translate("re_enter_password")}</Label>
                    <Input autoComplete="new-password"/>
                </TextField>
                {errorKey&&<ErrorMessage>{translate(errorKey)}</ErrorMessage>}
                <Button type="submit">{translate("sign_up")}</Button>
            </Form>
            <div className="flex items-center gap-3">
                <Separator className="flex-1" />
                <span className="text-sm text-muted">{translate("or")}</span>
                <Separator className="flex-1" />
            </div>
            <p>{translate("special_sign_in_later")}</p>
        </div>
    )
}
