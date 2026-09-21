import './Register.css'
/*import {LocalText} from "sharedLogic";
import {useState} from 'react'*/
import {getLanguage,localize} from "../getLanguage.ts";
import {FieldError,Form,Input,Label,TextField} from '@heroui/react';
export function Register(){
    const lang=getLanguage().toUpperCase()

   /* const [email,setEmail]=useState<string>("")
    const [login,setLogin]=useState<string>("")
    const [password,setPassword]=useState<string>("")
    const [repeatPassword,setRepeatPassword]=useState<string>("")*/
    const onSubmit = async (e: { preventDefault: () => void; }) => {
        e.preventDefault();
    }

        // @ts-ignore
    return (
        <div>
            <h1>{localize("create_account",lang)}</h1>
            <h2>{localize("sign_up_to_continue",lang)}</h2>
            <Form onSubmit={onSubmit}>
               {/* <TextField
                    isRequired
                    name="email"
                    type="email"
                >
                    <Label>Email</Label>
                    <Input placeholder="john@example.com" />
                    <FieldError />
                </TextField>*/}

            </Form>
        </div>
    )
}
