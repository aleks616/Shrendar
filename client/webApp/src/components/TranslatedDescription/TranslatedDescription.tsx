/// <reference types="vite/client" />

import {Button} from "@heroui/react"
import {LanguageIcon} from "@heroicons/react/24/solid"
import React,{useState} from "react"
import {getLanguage} from "../getLanguage.ts"

export function TranslatedDescription({description,translateDescription}: {
    description: string,
    translateDescription: () => void
}){
    const language=getLanguage()
    const [translatedDescription,setTranslatedDescription]=useState(description)

    const translate=async () => {
        const apiKey=import.meta.env.VITE_GEMINI_API_KEY
        if(!apiKey){
            throw new Error("VITE_GEMINI_API_KEY is not configured")
        }

        const response=await fetch(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-goog-api-key": apiKey
                },
                body: JSON.stringify({
                    contents: [{
                        parts: [{
                            text: `Translate this description into ${language}. Return only the translation. Preserve the original meaning, facts, tone, formatting, names, and line breaks. Do not summarize, rewrite, explain, add, or remove information.\n\n${description}`
                        }]
                    }]
                })
            }
        )

        if(!response.ok){
            throw new Error(`Gemini translation failed: ${response.status}`)
        }

        const data=await response.json()
        const translation=data.candidates?.[0]?.content?.parts?.[0]?.text
        if(typeof translation!=="string"||translation.length===0){
            throw new Error("Gemini returned no translation")
        }

        setTranslatedDescription(translation)
    }

    return (
        <div className={"border p-4 border-gray-500 rounded-md mt-3 relative"} style={{fontSize:"12pt"}}>
            <div className={"whitespace-pre-wrap pr-8"}>
                {translatedDescription}
            </div>
            <Button className={"absolute bottom-2 right-2"} onPress={translate} variant={"secondary"}>
                <LanguageIcon className={"size-6"}/>
            </Button>
        </div>
    )
}