import {BrowserRouter,Route,Routes} from "react-router-dom"
import {NotFound} from "./pages/NotFound/NotFound.tsx"
import {Register} from "./pages/Register"
import {Login} from "./pages/Login"
import {Home} from "./pages/Home"
import {ForgotPassword} from "./pages/ForgotPassword"
import {ProfilePage} from "./pages/ProfilePage"
import {SetPassword} from "./pages/SetPassword"
import {UserSettings} from "./pages/UserSettings"
import {BandWikiPage} from "./pages/BandWikiPage"
import {ArtistWikiPage} from "./pages/ArtistWikiPage";
import {AlbumWikiPage} from "./pages/AlbumWikiPage";
import {EventWikiPage} from "./pages/EventWikiPage";

const routes=[
    {path:'/',element:<Home/>},
    {path:'/register',element:<Register/>},
    {path:'/login',element:<Login/>},
    {path:'/forgot-password',element:<ForgotPassword/>},
    {path:'/reset-password',element:<SetPassword/>},
    {path:'/u/:user',element:<ProfilePage/>},
    {path:'/band/:band',element:<BandWikiPage/>},
    {path:'/artist/:artist',element:<ArtistWikiPage/>},
    {path:'/album/:album',element:<AlbumWikiPage/>},
    {path:'/event/:event',element:<EventWikiPage/>},
    {path:'/settings',element:<UserSettings/>},
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