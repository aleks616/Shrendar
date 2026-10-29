import {getLanguage,setLanguage} from "../Language.ts"
import {useState} from "react"
import {Button,Dropdown,Label} from "@heroui/react"
import {LanguageIcon} from "@heroicons/react/24/solid"

const languages:{code:string,selfname:string}[]=[
    {code:"EN",selfname:"English"},
    {code:"PL",selfname:"Polski"},
]

export function LanguageSwitcher(){
    return (
        <Dropdown>
            <Dropdown.Trigger>
                <div className={"inline-flex h-fit items-center gap-0.5 rounded-full p-0.5 border bg-background-secondary"}>
                    <div
                        className={"flex flex-center items-center justify-center size-8 rounded-full outline-none"}>
                        <LanguageIcon className={"relative z-10 size-5"}/>
                    </div>
                </div>
            </Dropdown.Trigger>
            <Dropdown.Popover>
                <Dropdown.Menu>
                    {languages.map(lang=>(
                        <Dropdown.Item key={lang.code}>
                               <Button variant={"ghost"}
                                       className={"margin-0 padding-0"}
                                       onPress={()=>{setLanguage(lang.code)}}
                               >
                                   <p className={"font-semibold "}>
                                       {lang.selfname}
                                   </p>
                               </Button>
                        </Dropdown.Item>
                    ))}
                </Dropdown.Menu>
            </Dropdown.Popover>
        </Dropdown>
    )
}