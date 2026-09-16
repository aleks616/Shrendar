// @ts-nocheck
import './Greeting.css'
import {useState} from 'react'
import {JSLogo} from '../JSLogo/JSLogo.tsx'
import {Greeting as KotlinGreeting} from 'sharedLogic'
import {LocalText} from 'sharedLogic'
import type {AnimationEvent} from 'react'
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
export function Greeting(){
    const greeting=new KotlinGreeting()
    const greeting1 = new LocalText().getString("greeting")
    const [isVisible,setIsVisible]=useState<boolean>(false)
    const [isAnimating,setIsAnimating]=useState<boolean>(false)
    const handleClick=() => {
        if(isVisible){
            setIsAnimating(true)
        }
        else{
            setIsVisible(true)
        }
    }

    const handleAnimationEnd=(event: AnimationEvent<HTMLDivElement>) => {
        if(event.animationName==='fadeOut'){
            setIsVisible(false)
            setIsAnimating(false)
        }
    }

    return (
        <div className="greeting-container">
            <button onClick={handleClick} className="greeting-button">
                Click me!
            </button>

            {isVisible&&(
                <div className={isAnimating?'greeting-content fade-out':'greeting-content'}
                     onAnimationEnd={handleAnimationEnd}>
                    <JSLogo/>
                    <div>React: {englishStrings[greeting1.stringRes_1.key_1]}</div>
                    <div style={{fontSize:"32px"}}>
                        <p>Page is development, check out my band website</p>
                        <p>Strona dopiero powstaje obczaj strone zespołu</p>
                    <a href={"https://mantikora-pl.github.io/"}>Mantikora</a>
                </div>
                </div>
            )}
        </div>
    )
}