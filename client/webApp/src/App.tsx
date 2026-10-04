import {CreatePassword} from "./components/CreatePassword/CreatePassword.tsx"
import {BrowserRouter,Route,Routes} from "react-router-dom"
import {UserProfile} from "./components/UserProfile/UserProfile.tsx";
import {NotFound} from "./pages/NotFound/NotFound.tsx";
import {Register} from "./pages/Register";
import {Login} from "./pages/Login";
import {Home} from "./pages/Home";
import {ForgotPassword} from "./pages/ForgotPassword";

const routes=[
    {path:'/',element:<Home/>},
    {path:'/register',element:<Register/>},
    {path:'/login',element:<Login/>},
    {path:'/forgot-password',element:<ForgotPassword/>},
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