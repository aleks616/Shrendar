import {CreatePassword} from "./components/CreatePassword/CreatePassword.tsx"
import {RequestPasswordReset} from "./components/RequestPasswordReset/RequestPasswordReset.tsx"
import {BrowserRouter,Route,Routes} from "react-router-dom"
import {UserProfile} from "./components/UserProfile/UserProfile.tsx";
import {NotFound} from "./pages/NotFound/NotFound.tsx";
import {SignIn} from "./components/SignIn/SignIn.tsx";

const routes=[
    {path:'/',element:<SignIn/>},
    {path:'/reset-password',element:<CreatePassword/>},
    {path:'/u/:user',element:<UserProfile/>},
    {path:'*',element:<NotFound/>},
]
export default function App(){
    return (
        <BrowserRouter>
            <Routes>
                {routes.map(({path,element})=>(
                    <Route key={path} path={path} element={element}/>
                ))}
            </Routes>
        </BrowserRouter>
    )
}