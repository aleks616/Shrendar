import {AccountClient,LoginRequestDto,UserDto} from "sharedLogic"
import React,{useEffect,useState} from "react"
import {
    Button,
    DateValue,
    ErrorMessage,
    FieldError,
    Form,
    Input,
    Label,
    Modal,
    Spinner,
    TextField
} from "@heroui/react"
import {Link} from "react-router-dom"
import {CalendarDate,getLocalTimeZone,today} from "@internationalized/date"
import {getLanguage} from "../Language.ts"
import {CalendarDateField} from "../CalendarDateField/CalendarDateField.tsx"

export function Settings({strings}: { strings: Record<string,string> }){
    const translate=(key: string): string => {
        return strings[key]??key
    }
    const toCalendarDate=(dateObj: {
        year: number,
        month: number,
        day: number
    } | null | undefined): CalendarDate | null => {
        if(!dateObj) return null
        return new CalendarDate(dateObj.year,dateObj.month,dateObj.day)
    }

    //todo reset password no code
    const [userData,setUserData]=useState(new UserDto())
    const [isLoading,setIsLoading]=useState(false)

    const [username,setUsername]=useState(userData.username??"")
    const isUsernameInvalid=username.length>0&&(username.length<4||username.length>50)
    const [email,setEmail]=useState(userData.email??"")
    const isEmailInvalid=email.length>0&& !email.includes("@")
    const minDate=today(getLocalTimeZone()).subtract({years: 120})
    const maxDate=today(getLocalTimeZone()).subtract({years: 13})
    const [birthdate,setBirthdate]=useState<DateValue | null>(null)
    const isBirthdateInvalid=birthdate!==null&&(birthdate<minDate||birthdate>maxDate)
    const [errorText,setErrorText]=useState("")

    const [modalEmail,setModalEmail]=useState("")
    const [login,setLogin]=useState("")
    const [password,setPassword]=useState("")
    const [modalOpen,setModalOpen]=useState(false)

    const handleBirthdateChange=(value: DateValue | null) => {
        setBirthdate(value)
    }

    const updateUsername=async () => {
        const token=localStorage.getItem("token")
        if(token!==null&&token!==undefined){
            const result=await AccountClient.getInstance().updateUsername(token,username)
            console.log(result)
            if(result!="username_changed"){
                setErrorText(translate(result))
            }
        }
    }

    const updateEmail=async () => {
        const token=localStorage.getItem("token")
        if(token!==null&&token!==undefined){
            const result=await AccountClient.getInstance().updateEmail(token,email)
            console.log(result)
            if(result!="email_changed"){
                setErrorText(translate(result))
            }
        }
    }

    const updateBirthdate=async () => {
        const token=localStorage.getItem("token")
        if(token!==null&&token!==undefined&&birthdate!==null){
            const date=birthdate as CalendarDate
            const result=await AccountClient.getInstance().addBirthday(token,date.toString())
            console.log(result)
            if(result!="birthday_added"){
                setErrorText(translate(result))
            }
            else{
                setErrorText("")
            }
        }
    }

    const submitChanges=async () => {
        if(username!==userData.username){
            await updateUsername()
        }
        if(email!==userData.email){
            await updateEmail()
        }
        if(birthdate!==userData.birthDate){
            await updateBirthdate()
        }
    }

    const deleteAccount=async () => {
        const request=new LoginRequestDto(login,modalEmail,password)
        const token=localStorage.getItem("token")
        setModalOpen(false)
        setModalEmail("")
        setLogin("")
        setPassword("")
        if(token!==null&&token!==undefined){
            const result=await AccountClient.getInstance().deleteAccount(token,request,getLanguage())
            console.log(result)
            if(result==="confirmed"){
                localStorage.removeItem("token")
                window.location.href="/"
            }
            else{
                setErrorText(translate(result))
            }
        }
    }

    useEffect(() => {
        const getUserData=async () => {
            setIsLoading(true)
            const token=localStorage.getItem("token")
            if(token!==null&&token!==undefined&&token.length>0){
                const result=await AccountClient.getInstance().getUserData(token)
                if(result.login!==null&&result.login!==undefined){
                    localStorage.setItem("login",result.login)
                    setUserData(result)
                    setIsLoading(false)
                    setUsername(result.username??"")
                    setEmail(result.email??"")
                    setBirthdate(toCalendarDate(result.birthDate))
                }
            }
        }
        getUserData()
    },[])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    return (
        <div className="flex flex-col gap-5">
            <TextField className={"w-64"} isInvalid={isUsernameInvalid}>
                <Label htmlFor={"username"}>{translate("username")}</Label>
                <Input
                    id="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <FieldError>{translate("username_invalid")}</FieldError>
            </TextField>

            <TextField className={"w-64"} isInvalid={isEmailInvalid} isDisabled={true}>
                <Label htmlFor={"email"}>{translate("email")}</Label>
                <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <FieldError>{translate("invalid_email")}</FieldError>
            </TextField>

            <CalendarDateField
                className={"w-64!"}
                name={"date"}
                isDisabled={false}
                minValue={minDate}
                maxValue={maxDate}
                value={birthdate}
                onChange={handleBirthdateChange}
                label={translate("birthdate")}
                fieldError={translate("invalid_user_birthdate")}
                description={null}
            />

            <ErrorMessage>{errorText}</ErrorMessage>

            <Button onPress={submitChanges} isDisabled={isUsernameInvalid||isEmailInvalid||isBirthdateInvalid}>
                {translate("save_changes")}
            </Button>

            <Link to={"/forgot-password"}>
                <Button variant={"secondary"}>{translate("change_password")}</Button>
            </Link>

            <Button onPress={() => setModalOpen(true)} variant={"danger-soft"}>
                {translate("delete_account")}
            </Button>

            <Modal.Backdrop isOpen={modalOpen} onOpenChange={setModalOpen}>
                <Modal.Container>
                    <Modal.Dialog className="sm:max-w-md">
                        <Modal.CloseTrigger/>
                        <Modal.Header>
                            <Modal.Heading>{translate("delete_account")}</Modal.Heading>
                        </Modal.Header>
                        <Modal.Body>
                            <Form className="flex flex-col gap-4">
                                <TextField isRequired name="email" value={modalEmail} onChange={setModalEmail}>
                                    <Label>{translate("email")}</Label>
                                    <Input autoComplete="email"/>
                                </TextField>
                                <TextField isRequired name="login" value={login} onChange={setLogin}>
                                    <Label>{translate("login")}</Label>
                                    <Input autoComplete="username"/>
                                </TextField>
                                <TextField
                                    isRequired
                                    name="password"
                                    type="password"
                                    value={password}
                                    onChange={setPassword}
                                >
                                    <Label>{translate("password")}</Label>
                                    <Input autoComplete="current-password"/>
                                </TextField>
                            </Form>
                        </Modal.Body>
                        <Modal.Footer>
                            <Button
                                onPress={deleteAccount}
                                variant={"danger"}
                                isDisabled={login.length===0||password.length===0}
                            >
                                {translate("delete_account")}
                            </Button>
                        </Modal.Footer>
                    </Modal.Dialog>
                </Modal.Container>
            </Modal.Backdrop>
        </div>
    )
}