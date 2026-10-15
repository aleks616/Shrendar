import {HomepageClient,UpcomingAnniversariesDto} from "sharedLogic"
import React,{useEffect,useState} from "react"
import {Heading,Link,Spinner} from "@heroui/react";

export function UpcomingAnniversaries({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [data,setData]=useState<UpcomingAnniversariesDto | null>(null)
    const [isLoading,setIsLoading]=useState(true)

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            const token=localStorage.getItem("token")
            const data=await HomepageClient.getInstance().getUpcoming(token)
            if(data!=null) setData(data)
            setIsLoading(false)
        }
        fetchData()
    },[])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(data==null){
        return <></>
    }

    const birthdays=data?.artistBirthdays?.asJsReadonlyArrayView()??[]
    const deathAnniversaries=data?.artistDeathAnns?.asJsReadonlyArrayView()??[]
    const albums=data?.albums?.asJsReadonlyArrayView()??[]

    return (
        <div className={"mx-3 my-2 flex flex-col gap-2"}>
            {albums.length>0&&
                <section className={"flex flex-col gap-4"}>
                    <Heading level={3}>{translate("upcoming_album_anniversaries")}</Heading>
                    {albums.map((album,index) => (
                        <div key={index}>
                            <Link href={"/band/"+album.bandId}>{album.bandName}</Link>
                            <span> - </span>
                            <Link href={"/album/"+album.id}>{album.title}</Link>
                            <p>{album.daysTillAnniversary} {translate("time_dp")} {translate("to")} {translate("anniversary")}</p>
                            <p>{album.age+1} {translate("time_yp")}</p>
                            <p>{album.releaseDate.toString()}</p>
                        </div>
                    ))}
                </section>}

            {birthdays.length>0&&
                <section className={"flex flex-col gap-4"}>
                    <Heading level={3}>{translate("upcoming_birthdays")}</Heading>
                    {birthdays.map((artist,index) => (
                        <div key={index}>
                            <Link href={"/artist/"+artist.id}>{artist.name}</Link>
                            <p>{artist.daysTillAnniversary} {translate("time_dp")} {translate("to")} {translate("birthday")}</p>
                            <p>{artist.yearsSince+1} {translate("time_yp")}</p>
                            <p>{artist.anniversaryDate.toString()}</p>
                        </div>
                    ))}
                </section>}

            {deathAnniversaries.length>0&&
                <section className={"flex flex-col gap-4"}>
                    <Heading level={3}>{translate("upcoming_death_anniversaries")}</Heading>
                    {deathAnniversaries.map((artist,index) => (
                        <div key={index}>
                            <Link href={"/artist/"+artist.id}>{artist.name}</Link>
                            <p>{artist.daysTillAnniversary} {translate("time_dp")} {translate("to")} {translate("death_anniversary")}</p>
                            <p>{artist.yearsSince+1} {translate("time_yp")}</p>
                            <p>{artist.anniversaryDate.toString()}</p>
                        </div>
                    ))}
                </section>}
        </div>
    )
}