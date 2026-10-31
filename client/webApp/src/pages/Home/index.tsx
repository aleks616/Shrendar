import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import {getLanguage} from "../../components/Language.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {CommonBands} from "../../components/CommonBands/CommonBands.tsx"
import {UpcomingAnniversaries} from "../../components/UpcomingAnniversaries/UpcomingAnniversaries.tsx"
import React,{useEffect,useState} from "react"
import {isMobile} from "react-device-detect"
import {HomepageData} from "../../components/HomepageData/HomepageData.tsx"

export function Home() {
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings

    const [mobile,setMobile]=useState(isMobile||window.innerWidth<900)
    useEffect(() => {
        const handleResize=() => {
            setMobile(window.innerWidth<900)
        }
        window.addEventListener("resize",handleResize)
        return () => {
            window.removeEventListener("resize",handleResize)
        }
    },[])

    return (
        <>
            <AppHeader />
            <div className={"flex justify-center w-svw"}>
                <div className={"flex flex-col min-w-svw"}>
                    <div className={"flex min-w-svw"}>
                        {!mobile&&<>
                            <aside className={"bg-background-secondary w-2/12 h-full rounded-lg"}>
                                <UpcomingAnniversaries strings={strings}/>
                            </aside>
                        </>}
                        <HomepageData strings={strings}/>
                        {!mobile&&<>
                            <aside className={"bg-background-secondary w-2/12 h-full rounded-lg"}>
                                <CommonBands strings={strings}/>
                            </aside>
                        </>}
                    </div>
                    {mobile&&<>
                        <UpcomingAnniversaries strings={strings}/>
                        <CommonBands strings={strings}/>
                    </>}
                </div>
            </div>
        </>
    )
}