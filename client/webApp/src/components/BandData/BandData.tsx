import React,{useEffect,useState} from 'react'
import {BandClient,BandWikiDto} from "sharedLogic"
import {Navigate,useParams} from "react-router-dom"
import {Button,Heading,Spinner,Tooltip} from "@heroui/react"
import {InformationCircleIcon} from "@heroicons/react/24/outline"
import {isMobile} from "react-device-detect"
import {TranslatedDescription} from "../TranslatedDescription/TranslatedDescription.tsx";

export function BandData({strings}: { strings: Record<string,string> }){
    const [band,setBand]=useState<BandWikiDto | null>(new BandWikiDto())
    const [isLoading,setIsLoading]=useState(true)
    const translate=(key: string) => strings[key]??key
    const params=useParams()
    const bandIdParam=parseInt(params.band?params.band:"0")

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            if(bandIdParam==null) return
            const bandData=await BandClient.getInstance().getBandWikiPageDataById(bandIdParam)
            setIsLoading(false)
            if(bandData!=null){
                setBand(bandData)
            }
            else{
                setBand(null)
            }
        }
        fetchData()
    },[bandIdParam])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(band==null){
        return <Navigate to="/404"/>
    }

    const bandMembersList=band.bandMembers!.asJsReadonlyArrayView()
    const bandGenres=band.computedGenres!.asJsReadonlyArrayView()
    const albumsList=band.albums!.asJsReadonlyArrayView()
    const similarBandsList=band.similar!.asJsReadonlyArrayView()

    const statusColor=band.status==="Active"?"text-success":band.status==="Disbanded"?"text-error":"text-warning"
    return (
        <div className={"flex flex-col gap-4 w-5xl mx-3"}>
            <Heading level={1}>{band.name}</Heading>
            <div className={"flex w-full min-h-32 text-lg gap-4"}>
                <div className={"flex flex-col"+(!isMobile?" w-9/12":"")}>
                    <div className={"flex min-h-32 text-lg"}>
                        <div className={"w-5/9"}>
                            <p>
                                <span className={"text-muted"}>Country: </span>
                                {translate(band.country!)}
                            </p>
                            <p>
                                <span className={"text-muted"}>Status: </span>
                                <span>{band.status}</span>
                            </p>
                            <p>
                                <span className={"text-muted"}>Years active: </span>
                                {band.formedYear} - {band.disbandedYear??"present"}
                            </p>
                        </div>
                        <div className={"w-4/9"}>
                            <span className={"text-muted"}>Top genres: </span>
                            <Tooltip delay={0}>
                                <Tooltip.Trigger>
                                    <Button isIconOnly aria-label="More information" variant="ghost"
                                            className={"border-none shadow-none h-auto w-auto min-w-0 p-0 inline-flex align-text-bottom"}>
                                        <InformationCircleIcon/>
                                    </Button>
                                </Tooltip.Trigger>
                                <Tooltip.Content>
                                    <p>these are computed automatically based on album's genres and importance, so they
                                        might be unexpected</p>
                                </Tooltip.Content>
                            </Tooltip>
                            {bandGenres.map((genre,index) =>
                                <p key={index}>{genre.name} </p>
                            )}
                        </div>
                    </div>

                    {isMobile&&<div className={"max-w-xl flex justify-center my-6"}>
                        <img src={band.imageUrl!} alt={band.name!} className={"rounded-md"}/>
                    </div>}

                    <TranslatedDescription
                        description={band.description??""}
                        translateDescription={() => {}}
                    />
                </div>
                {!isMobile&&
                    <div className={"size-72"}>
                        <img src={band.imageUrl!} alt={band.name!} className={"rounded-md"}/>
                    </div>
                }
            </div>
            <div className={isMobile?"w-96":"w-full"}>


            </div>


        </div>
    )
}