import React,{useEffect,useState} from "react"
import {EventClient,EventWikiDto} from "sharedLogic"
import {Navigate,useParams} from "react-router-dom"
import {Heading,Link,Spinner} from "@heroui/react"
import {TranslatedDescription} from "../TranslatedDescription/TranslatedDescription.tsx"

export function EventData({strings}: { strings: Record<string,string> }){
    const [event,setEvent]=useState<EventWikiDto | null>(new EventWikiDto())
    const [isLoading,setIsLoading]=useState(true)

    const translate=(key: string) => strings[key]??key
    const params=useParams()
    const eventIdParam=parseInt(params.event?params.event:"0")

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            if(eventIdParam==null) return
            const eventData=await EventClient.getInstance().getEventData(eventIdParam)
            if(eventData!=null){
                setEvent(eventData)
            }
            else{
                setEvent(null)
            }
            setIsLoading(false)
        }
        fetchData()
    },[eventIdParam])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(event==null){
        return <Navigate to="/404"/>
    }

    return (
        <div className={"flex flex-col gap-4 w-5xl mx-3"}>
            <header className={"flex gap-2"}>
                <Heading level={1}>{event.name}</Heading>
            </header>
            <main className={"w-full min-h-32 gap-4"}>
                    <section className={"min-h-24"}>
                        <p>
                            <span className={"text-muted"}>{translate("band")}: </span>
                            <Link href={`../band/${event.bandId}`}>{event.bandName??"-"}</Link>
                        </p>
                        <p>
                            <span className={"text-muted"}>{translate("date")}: </span>
                            {event.date?event.date.toString():"-"}
                            <span className={"text-muted"}> {translate("anniversary_in")} </span>
                            {event.daysTillAnniversary?event.daysTillAnniversary:"-"}
                            <span> {translate("days")}</span>
                        </p>
                        <p>
                            <span className={"text-muted"}>{translate("years_since")}: </span>
                            {event.yearsSince??"-"}
                        </p>
                    </section>
                    <TranslatedDescription
                        description={event.description??""}
                        translateDescription={() => {
                        }}
                    />
            </main>
        </div>
    )
}