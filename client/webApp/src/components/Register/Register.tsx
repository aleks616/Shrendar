import './Register.css'
import {getLanguage} from "../getLanguage.ts"
import {Button,Form,Input,Label,TextField,Heading,ErrorMessage,Link,InputOTP,REGEXP_ONLY_DIGITS} from '@heroui/react'
import {RegisterClient,RegisterRequestDto,RegisterValidator} from "sharedLogic"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {useEffect,useState} from "react"
import LabelledDivider from "../LabelledDivider/LabelledDivider.tsx"

//GenreClient.getInstance().getAll().then((genres)=>console.log(genres))
export function Register(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key:string)=>strings[key]??key

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
        setResendCountdown(60)
        const validator=new RegisterValidator()
        try{
            const registerError=await new RegisterValidator().validateLogin(login)
            if(registerError){
                setErrorKey(registerError)
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
            setCodeSent(true)
            setTimerOn(true)
            const registerRequest=new RegisterRequestDto(login,login,email,password,lang)
            const result=await RegisterClient.getInstance().register(registerRequest)
            if(result=="verification_code_sent"){
                setErrorKey(null)
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
                setErrorKey(null)
            }
            else{
                setErrorKey(confirmationResult)
            }
        }catch(e){
            console.log(e)
            setErrorKey("something_wrong")
        }
    }

    useEffect(() => {
        if(!timerOn){
            return
        }

        const timeoutId=setTimeout(()=>{
            if(resendCountdown>0){
                setResendCountdown(currentCountdown=>currentCountdown-1)
            }
            else{
                setTimerOn(false)
            }
        },1000)

        return()=>clearTimeout(timeoutId)
    },[timerOn,resendCountdown])

    return (
        <div>
            <Heading level={1}>{translate("create_account")}</Heading>
            <Heading level={3}>{translate("sign_up_to_continue")}</Heading>
            <Form className="flex w-96 flex-col gap-4">
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
                    <Button
                        isDisabled={email.length===0||login.length===0||password.length===0
                            ||repeatPassword.length===0||confirmed||(resendCountdown>0&&codeSent)}
                        onPress={createAccount}>{translate("sign_up")}</Button>
                </div>

                {(codeSent&&!confirmed)&&<>
                <p>{translate("verification_code_sent")}</p>
                    <div className={"flex justify-center gap-1"}>
                        <p>{translate("no_code")} </p>
                        <Link onPress={createAccount} isDisabled={timerOn}>
                            <span>{timerOn?translate("resend_code_in"):translate("resend_code")}&nbsp;</span>
                            {timerOn&&<span>{resendCountdown}</span>}
                        </Link>
                    </div>

                    <div className={"flex justify-center"}>
                        <InputOTP
                            pattern={REGEXP_ONLY_DIGITS}
                            value={code}
                            onChange={(val) => {
                                setCode(val)
                            }}
                            className="w-fit"
                            maxLength={6}
                        >
                            <InputOTP.Group>
                                <InputOTP.Slot index={0}/>
                                <InputOTP.Slot index={1}/>
                                <InputOTP.Slot index={2}/>
                                <InputOTP.Slot index={3}/>
                                <InputOTP.Slot index={4}/>
                                <InputOTP.Slot index={5}/>
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
