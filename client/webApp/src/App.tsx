import {CreatePassword} from "./components/CreatePassword/CreatePassword.tsx"
import {RequestPasswordReset} from "./components/RequestPasswordReset/RequestPasswordReset.tsx"
import {BrowserRouter,Route,Routes} from "react-router-dom"

const routes=[
    {path:'/',element:<RequestPasswordReset/>},
    {path:'/reset-password',element:<CreatePassword/>},
    //{path:'*',element:<NotFound/>},
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