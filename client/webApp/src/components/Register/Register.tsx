import './Register.css'
import {getLanguage} from "../getLanguage.ts";
import {Button,Form,Input,Label,TextField,Heading,ErrorMessage,Link,InputOTP} from '@heroui/react';
import {RegisterClient,RegisterRequestDto,RegisterValidator} from "sharedLogic";
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json';
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json';
import {useState} from "react";
import LabelledDivider from "../LabelledDivider/LabelledDivider.tsx";

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

    const [code,setCode]=useState("")
    const [codeSent,setCodeSent]=useState(false)
    const [timerOn,setTimerOn]=useState(false)
    const [resendCountdown,setResendCountdown]=useState(60)
    const [confirmed,setConfirmed]=useState(false)

    const createAccount=async () => {
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
            const registerRequest=new RegisterRequestDto(login,login,email,password,lang)
            const result=await RegisterClient.getInstance().register(registerRequest)
            if(result=="verification_code_sent"){
                setErrorKey(null)
                setCodeSent(true)
                setTimerOn(true)
                setResendCountdown(60)
            }
            else if(result=="something_wrong"){
                setErrorKey("something_wrong")
            }
        }
        catch(e){
            console.log(e)
            setErrorKey("something_wrong")
        }
    }

    const confirmAccount=async ()=>{
        try{
            const registerRequest=new RegisterRequestDto(login,login,email,password,lang)
            const confirmationResult=await RegisterClient.getInstance().registerConfirm(registerRequest,code)
            if(confirmationResult=="account_created"){
                setConfirmed(true)
                setTimerOn(false)
                //setCodeSent(false)
            }
            else{
                setErrorKey(confirmationResult)
            }
        }
        catch(e){
            console.log(e)
            setErrorKey("something_wrong")
        }
    }

    if(timerOn){
        setTimeout(()=>{
            if(resendCountdown>0){
                setResendCountdown(resendCountdown-1)
            }
            else{
                setTimerOn(false)
            }
        },1000)
    }

    return (
        <div>
            <Heading level={1}>{translate("create_account")}</Heading>
            <Heading level={3}>{translate("sign_up_to_continue")}</Heading>
            <Form className="flex w-96 flex-col gap-4" onSubmit={createAccount}>
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
                <div className={"flex justify-center"}>
                    <Button isDisabled={email.length===0||login.length===0||password.length===0||repeatPassword.length===0||confirmed||(resendCountdown>0&&codeSent)}
                        onPress={createAccount}>{translate("sign_up")}</Button>
                </div>
                {codeSent&&<>
                <p>{translate("verification_code_sent")}</p>
                    {timerOn&&<p>
                        {translate("resend_code_in")} {resendCountdown}
                    </p>
                    }
                    <div className={"flex justify-center gap-1"}>
                        <p>Didn't receive the code? </p>
                        <Link onPress={createAccount} isDisabled={resendCountdown>0}>
                            {translate("resend_code")}
                        </Link>
                    </div>

                    <div className={"flex justify-center"}>
                        <InputOTP maxLength={6}>
                            <InputOTP.Group>
                                <InputOTP.Slot index={0} />
                                <InputOTP.Slot index={1} />
                                <InputOTP.Slot index={2} />
                                <InputOTP.Slot index={3} />
                                <InputOTP.Slot index={4} />
                                <InputOTP.Slot index={5} />
                            </InputOTP.Group>
                        </InputOTP>
                    </div>

                    <div className={"flex justify-center"}>
                        <Button onPress={confirmAccount} isDisabled={code.length<6}>
                            {translate("confirm_account")}
                        </Button>
                    </div>
                </>
                }

                {confirmed&&<p>
                    {translate("account_created")}
                </p>
                }
            </Form>
            <LabelledDivider text={translate("or")}/>
            <p>{translate("special_sign_in_later")}</p>
        </div>
    )
}
