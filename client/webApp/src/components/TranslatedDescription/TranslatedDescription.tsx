import {Button} from "@heroui/react"
import {LanguageIcon} from "@heroicons/react/24/solid"
import React,{useState} from "react"
import {getLanguage} from "../getLanguage.ts"
import {LlmClient,TranslationRequestDto} from "sharedLogic"

export function TranslatedDescription({description,translateDescription}: {
    description: string,
    translateDescription: () => void
}){
    const language=getLanguage()
    const [translatedDescription,setTranslatedDescription]=useState(description)

    const translate=async () => {
        const translationRequest=new TranslationRequestDto(description,language)
        const translation=await LlmClient.getInstance().translate(translationRequest)
        setTranslatedDescription(translation)
    }

    return (
        <div className={"border p-4 border-gray-500 rounded-md mt-3 relative"}>
            <div className={"whitespace-pre-wrap pr-8"}>
                {translatedDescription}
            </div>
            <Button className={"absolute bottom-2 right-2"} onPress={translate} variant={"secondary"}>
                <LanguageIcon className={"size-6"}/>
            </Button>
        </div>
    )
}