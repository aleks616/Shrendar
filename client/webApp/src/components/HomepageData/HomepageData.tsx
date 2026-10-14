import {HomepageClient,HomepageMainDto} from "sharedLogic"
import React,{useEffect,useState} from "react"
import {Heading,Spinner} from "@heroui/react"
import {AlbumAnniversaryCard} from "../AlbumAnniversaryCard/AlbumAnniversaryCard.tsx"
import {Navigate} from "react-router-dom"
import {ArtistBirthdayCard} from "../ArtistBirthdayCard/ArtistBirthdayCard.tsx"
import {ArtistDeathAnniversaryCard} from "../ArtistDeathAnniversaryCard/ArtistDeathAnniversaryCard.tsx"
import {EventAnniversaryCard} from "../EventAnniversaryCard/EventAnniversaryCard.tsx";
import {isMobile} from "react-device-detect";
import {UpcomingAnniversaries} from "../UpcomingAnniversaries/UpcomingAnniversaries.tsx";
import {CommonBands} from "../CommonBands/CommonBands.tsx";

export function HomepageData({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [data,setData]=useState<HomepageMainDto | null>(null)
    const [isLoading,setIsLoading]=useState(true)

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            const token=localStorage.getItem("token")
            const data=await HomepageClient.getInstance().getTodayAnniversaries(token)
            if(data!=null) setData(data)
            setIsLoading(false)
        }
        fetchData()
    },[])

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

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(data==null){
        return <Navigate to="/404" replace={false}/>
    }

    const favoriteAlbums=data?.favoriteAlbums?.asJsReadonlyArrayView()??[]
    const favoriteArtistsBirthdays=data?.favoriteArtistsBirthdays?.asJsReadonlyArrayView()??[]
    const favoriteArtistsDeathAnns=data?.favoriteArtistsDeathAnniversaries?.asJsReadonlyArrayView()??[]

    const otherBandMembersBirthdays=data?.otherBandMembersBirthdays?.asJsReadonlyArrayView()??[]
    const otherBandMembersDeathAnns=data?.otherBandMembersDeathAnniversaries?.asJsReadonlyArrayView()??[]

    const recommendedArtistBirthdays=data?.recommendedArtistBirthdays?.asJsReadonlyArrayView()??[]
    const recommendedArtistDeathAnns=data?.recommendedArtistDeathAnniversaries?.asJsReadonlyArrayView()??[]
    const recommendedAlbums=data?.recommendedAlbumsAnniversaries?.asJsReadonlyArrayView()??[]

    const eventAnniversaries=data?.eventAnniversaries?.asJsReadonlyArrayView()??[]

    const albumCount=favoriteAlbums?.length??0+recommendedAlbums?.length??0
    const birthdayCount=favoriteArtistsBirthdays?.length??0+otherBandMembersBirthdays?.length??0+recommendedArtistBirthdays?.length??0
    const deathAnniversaryCount=favoriteArtistsDeathAnns?.length??0+otherBandMembersDeathAnns?.length??0+recommendedArtistDeathAnns?.length??0

    return (
        <div className={"flex flex-col"}>
            <div className={"flex justify-center items-center w-full"}>
                {!mobile&&<>
                    <aside className={"bg-background-secondary min-w-2/12 h-full rounded-lg"}>
                        <UpcomingAnniversaries strings={strings}/>
                    </aside>
                </>}
                <main className={"lg: max-w-8/12"}>
                    {albumCount>0&&
                        <>
                            <Heading level={3} className={"text-center"}>
                                {translate("album_anniversaries")}
                            </Heading>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {favoriteAlbums?.map((album,index) => (
                                    <AlbumAnniversaryCard key={index} strings={strings} album={album}/>
                                ))}
                            </div>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {recommendedAlbums?.map((album,index) => (
                                    <AlbumAnniversaryCard key={index} strings={strings} album={album}/>
                                ))}
                            </div>
                        </>
                    }

                    {birthdayCount>0&&
                        <>
                            <Heading level={3} className={"text-center"}>{translate("birthdays_today")}</Heading>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {favoriteArtistsBirthdays?.map((artist,index) => (
                                    <ArtistBirthdayCard key={index} strings={strings} artist={artist}/>
                                ))}
                            </div>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {otherBandMembersBirthdays?.map((artist,index) => (
                                    <ArtistBirthdayCard key={index} strings={strings} artist={artist}/>
                                ))}
                            </div>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {recommendedArtistBirthdays?.map((artist,index) => (
                                    <ArtistBirthdayCard key={index} strings={strings} artist={artist}/>
                                ))}
                            </div>
                        </>
                    }
                    {deathAnniversaryCount>0&&
                        <>
                            <Heading level={3} className={"text-center"}>{translate("died_today")}</Heading>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {favoriteArtistsDeathAnns?.map((artist,index) => (
                                    <ArtistDeathAnniversaryCard key={index} strings={strings} artist={artist}/>
                                ))}
                            </div>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {otherBandMembersDeathAnns?.map((artist,index) => (
                                    <ArtistDeathAnniversaryCard key={index} strings={strings} artist={artist}/>
                                ))}
                            </div>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {recommendedArtistDeathAnns?.map((artist,index) => (
                                    <ArtistDeathAnniversaryCard key={index} strings={strings} artist={artist}/>
                                ))}
                            </div>
                        </>
                    }
                    {eventAnniversaries.length>0&&
                        <>
                            <Heading level={3} className={"text-center"}>{translate("events_today")}</Heading>
                            <div className={"flex flex-wrap justify-center items-center"}>
                                {eventAnniversaries?.map((event,index) => (
                                    <EventAnniversaryCard key={index} strings={strings} event={event}/>
                                ))}
                            </div>
                        </>
                    }

                </main>
                {!mobile&&<>
                    <aside className={"bg-background-secondary min-w-2/12 h-full rounded-lg"}>
                        <CommonBands strings={strings}/>
                    </aside>
                </>}
            </div>
            {mobile&&<>
                <UpcomingAnniversaries strings={strings}/>
                <CommonBands strings={strings}/>
            </>}
        </div>
    )
}