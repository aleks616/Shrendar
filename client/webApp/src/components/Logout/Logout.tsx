import {AccountClient} from "sharedLogic"

export const logout=async () => {
    try{
        const token=localStorage.getItem("token")
        if(token){
            const result=await AccountClient.getInstance().logout(token)
            if(result=="logged_out"){
                localStorage.removeItem("token")
                localStorage.removeItem("login")
                window.location.reload()
            }
            else{
                return result
            }
        }
        else{
            return "no_token"
        }
    }
    catch(e){
        console.log(e)
    }
}

