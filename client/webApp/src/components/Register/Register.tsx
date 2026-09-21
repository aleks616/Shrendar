import './Register.css'
import {getLanguage} from "../getLanguage.ts";
import {Button,Form,Input,Label,TextField} from '@heroui/react';
import {RegisterAccount,RegisterValidator} from "sharedLogic";
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json';
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json';
import {useState} from "react";
import type {FormEvent} from "react";

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
            console.log(login)
            const loginError=await validator.validateLogin(login)
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
            await new RegisterAccount().register({
                login,
                displayName: login,
                email,
                password,
                language: lang
            })
        }
        catch(e){
            console.log(e)
            setErrorKey("unexpected_error")
        }
    }

    return (
        <div>
            <h1>{translate("create_account")}</h1>
            <h2>{translate("sign_up_to_continue")}</h2>
            <Form onSubmit={onSubmit}>
                <TextField name="email" type="email" value={email} onChange={setEmail}>

                    <Label>{translate("email_address")}</Label>
                    <Input autoComplete="email"/>

                </TextField>
                <TextField name="login" value={login} onChange={setLogin}>

                    <Label>{translate("login")}</Label>
                    <Input autoComplete="username"/>

                </TextField>
                <TextField name="password" type="password" value={password} onChange={setPassword}>

                    <Label>{translate("password")}</Label>
                    <Input autoComplete="new-password"/>

                </TextField>
                <TextField name="repeatPassword" type="password" value={repeatPassword} onChange={setRepeatPassword}>
                    <Label>{translate("re_enter_password")}</Label>
                    <Input autoComplete="new-password"/>
                </TextField>
                {errorKey&&<p role="alert">{translate(errorKey)}</p>}
                <Button type="submit">{translate("sign_up")}</Button>
            </Form>
            <p>{translate("or")}</p>
            <p>{translate("special_sign_in_later")}</p>
        </div>
    )
}
