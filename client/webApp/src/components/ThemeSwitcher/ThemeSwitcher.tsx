import React,{useEffect,useState} from "react"
import {Button,useTheme} from "@heroui/react"
import {MoonIcon,SunIcon,ComputerDesktopIcon} from '@heroicons/react/24/solid'

type ThemeOption="light" | "dark" | "system"

const themes: { id: ThemeOption;label: string;icon: React.ElementType }[]=[
    {id: "light",label: "Light",icon: SunIcon},
    {id: "dark",label: "Dark",icon: MoonIcon},
    {id: "system",label: "System",icon: ComputerDesktopIcon},
]

export function ThemeSwitcher(){
    const [mounted,setMounted]=useState(false)
    const {theme,setTheme}=useTheme("system")

    useEffect(() => {
        setMounted(true)
    },[])

    if(!mounted) return null

    return (
        <div className="flex items-center">
            <div
                role="radiogroup"
                aria-label="Theme selector"
                className="inline-flex h-fit items-center gap-0.5 rounded-full p-0.5 border bg-background-secondary"
            >
                {themes.map((item) => {
                    const isSelected=theme===item.id
                    const IconComponent=item.icon
                    return (
                        <Button
                            key={item.id}
                            aria-checked={isSelected}
                            aria-label={item.label}
                            variant="ghost"
                            isIconOnly
                            onPress={() => setTheme(item.id)}
                            className={`relative size-8 rounded-full p-0! outline-none focus-visible:ring-2 focus-visible:ring-focus ${
                                isSelected
                                    ?"bg-overlay! hover:bg-overlay!"
                                    :"bg-secondary! hover:bg-secondary!"
                            }`}
                        >
                            <IconComponent
                                className={"relative z-10 size-5"}
                            />
                        </Button>
                    )
                })}
            </div>
        </div>
    )
}