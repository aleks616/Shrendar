import {Description,FieldError,Input,Label,TextField} from "@heroui/react"
import React,{useState} from "react"

export function ImageField({imageUrl,onChange,placeholder,description,strings}: {
    imageUrl: string
    onChange: (imageUrl: string) => void
    placeholder: string
    description: string
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const [isImageValid,setIsImageValid]=useState(true)

    function invalidateImage(){
        setIsImageValid(false)
    }

    function handleUrlChange(e: any){
        onChange(e)
        setIsImageValid(true)
    }

    return (
        <>
            <TextField
                name={"imageUrl"}
                type={"text"}
                value={imageUrl}
                onChange={(e) => handleUrlChange(e)}
                isInvalid={imageUrl.length>255|| !isImageValid}
            >
                <Label>
                    {translate("image_url")}
                </Label>
                <Input placeholder={placeholder}/>
                <FieldError>{translate("url_too_long")}</FieldError>
                <Description>{description}</Description>
            </TextField>

            {imageUrl.length>0?
                <div className={"max-w-72"}>
                    <img src={imageUrl} alt={"preview"} onError={invalidateImage}/>
                </div>
                :
                <div className={"w-72 h-72 bg-background-secondary border-2"}/>
            }
        </>
    )
}
