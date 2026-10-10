import React,{useEffect,useState} from "react"
import {AlbumClient,AlbumWikiDto} from "sharedLogic"
import {Navigate,useParams} from "react-router-dom"
import {Heading,Spinner} from "@heroui/react";
import {isMobile} from "react-device-detect";
import {TranslatedDescription} from "../TranslatedDescription/TranslatedDescription.tsx";

export function AlbumData({strings}: { strings: Record<string,string> }){
    const [album,setAlbum]=useState<AlbumWikiDto | null>(new AlbumWikiDto())
    const [isLoading,setIsLoading]=useState(true)

    const translate=(key: string) => strings[key]??key
    const params=useParams()
    const albumIdParam=BigInt(params.album?params.album:"0")

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            if(albumIdParam==null) return
            const albumData=await AlbumClient.getInstance().getAlbumWikiPageData(albumIdParam)
            if(albumData!=null){
                setAlbum(albumData)
            }
            else{
                setAlbum(null)
            }
            setIsLoading(false)
        }
        fetchData()
    },[albumIdParam])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(album==null){
        return <Navigate to="/404"/>
    }


    return (
        <div className={"flex flex-col gap-4 w-5xl mx-3"}>
            <header className={"flex gap-2"}>
                <Heading level={1}>{album.albumName}</Heading>
            </header>
            <main className={"flex w-full min-h-32 gap-6"}>
                <div className={"flex flex-col"+(!isMobile?" w-9/12":"")}>
                    <section className={"flex min-h-28"}>
                        <div className={"w-5/9"}>
                            <p>
                                <span className={"text-muted"}>{translate("band")}: </span>
                                {album.band?.name?translate(album.band.name):"-"}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("release_date")}: </span>
                                {album.releaseDate?album.releaseDate.toString():"-"}
                                <span className={"text-muted"}> {translate("anniversary_in")} </span>
                                {album.daysTillAnniversary?album.daysTillAnniversary:"-"}
                                <span> days</span>
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("years_since")}: </span>
                                {album.albumAge}
                            </p>
                        </div>
                        <div className={"w-4/9"}>
                            <p>
                                <span className={"text-muted"}>{translate("album_type")}: </span>
                                {album.type}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("genre")}: </span>
                                {album.genre?album.genre.name:"-"}
                            </p>
                        </div>
                    </section>
                    {isMobile&&
                        <div className={"size-72"}>
                            <img src={album.artworkUrl!} alt={album.albumName!} className={"rounded-md"}/>
                        </div>
                    }
                    <TranslatedDescription
                        description={album.description??""}
                        translateDescription={() => {
                        }}
                    />
                </div>
                {!isMobile&&
                    <div className={"size-72"}>
                        <img src={album.artworkUrl!} alt={album.albumName!} className={"rounded-md"}/>
                    </div>
                }
            </main>

        </div>
    )
}