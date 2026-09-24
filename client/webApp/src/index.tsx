import React from 'react'
import ReactDOM from 'react-dom/client'
import {Register} from './components/Register/Register.tsx'
import {SignIn} from "./components/SingIn/SignIn.tsx";

const rootElement=document.getElementById('root')
if(!rootElement) throw new Error('Failed to find the root element')

ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
        <Register/>
        <SignIn/>
    </React.StrictMode>
)