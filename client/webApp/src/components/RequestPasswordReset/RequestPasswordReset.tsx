import {getLanguage} from "../getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {useEffect,useState} from "react"
import {Button,Form,Input,Label,TextField,Heading,ErrorMessage,Link} from '@heroui/react'
import {AccountClient} from "sharedLogic";

export function RequestPasswordReset(){
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key: string) => strings[key]??key
    const [login,setLogin]=useState("")
    const [errorText,setErrorText]=useState<string | null>(null)
    const [codeSent,setCodeSent]=useState(false)
    const [timerOn,setTimerOn]=useState(false)
    const [resendCountdown,setResendCountdown]=useState(60)

    const requestPasswordReset=async () => {
        setResendCountdown(60)
        try{
            const result=await AccountClient.getInstance().requestPasswordReset(login,lang)
            if(result!="password_link_sent"){
                if(result=="too_many_user_requests"){
                    setResendCountdown(240)
                    setTimerOn(true)
                }
                setErrorText(translate(result))
                return
            }
            setErrorText(null)
            setCodeSent(true)
            setTimerOn(true)
            return
        }
        catch(e){
            console.log(e)
            setErrorText(translate("something_wrong"))
        }
    }

    useEffect(() => {
        if(!timerOn){
            return
        }
        const timeoutId=setTimeout(() => {
            if(resendCountdown>0){
                setResendCountdown(currentCountdown => currentCountdown-1)
            }
            else{
                setTimerOn(false)
            }
        },1000)

        return () => clearTimeout(timeoutId)
    },[timerOn,resendCountdown])

    return (
        <div>
            <Heading level={2}>{translate("forgot_password_question")}</Heading>
            <Form className="flex w-96 flex-col gap-4">
                <TextField isRequired name="login" value={login} onChange={setLogin}>
                    <Label>{translate("login_email")}</Label>
                    <Input autoComplete={"email"}/>
                </TextField>
                {errorText&&<ErrorMessage>{errorText}</ErrorMessage>}
                <Button
                    isDisabled={login.length==0||timerOn}
                    onPress={requestPasswordReset}>
                    {translate("reset_password")}
                </Button>
                {codeSent&&<div>
                    <p>{translate("password_link_sent")}</p>
                    <div className={"flex justify-center gap-1"}>
                        <p>{translate("no_code")} </p>
                        <Link onPress={requestPasswordReset} isDisabled={timerOn}>
                            <span>{timerOn?translate("resend_code_in"):translate("resend_code")}&nbsp;</span>
                            {timerOn&&<span>{resendCountdown}</span>}
                        </Link>
                    </div>
                </div>}
            </Form>
        </div>
    )
}