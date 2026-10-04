import {AccountClient} from "sharedLogic"
import {GoogleLogin,GoogleOAuthProvider} from "@react-oauth/google"
import React,{useState} from "react"
import {Navigate} from "react-router-dom"

export function SignInWithGoogle(){
    const [redirect,setRedirect]=useState(false)
    const signInWithGoogle=async (credential: string) => {
        try{
            const result=await AccountClient.getInstance().authWithGoogle(credential)
            try{
                const token=JSON.parse(result).token
                localStorage.setItem("token",token)
                localStorage.removeItem("login")
                setRedirect(true)
            }
            catch(e){
                console.log(result)
            }
        }
        catch(e){
            console.log(e)
        }
    }

    if(redirect){
        return <Navigate to={"/"}/>
    }

    return(
        <GoogleOAuthProvider clientId="13978966379-vdg146pscvqtdotp98lqvjnrj9lik4nf.apps.googleusercontent.com">
            <GoogleLogin
                onSuccess={credentialResponse => {
                    if(credentialResponse.credential!=null) signInWithGoogle(credentialResponse.credential)
                }}
                onError={() => {
                    console.log('Login Failed')
                }}
            />
        </GoogleOAuthProvider>
    )
}