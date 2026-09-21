import React from 'react'
import ReactDOM from 'react-dom/client'
//import {GenreClient} from 'sharedLogic'
import {Register} from './components/Register/Register.tsx'

const rootElement=document.getElementById('root')
if(!rootElement) throw new Error('Failed to find the root element')

/*GenreClient.getInstance().getAll()
    .then((genres) => console.log(genres))*/

ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
        <Register/>
    </React.StrictMode>
)