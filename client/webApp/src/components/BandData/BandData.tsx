import React,{useEffect,useState} from 'react'
import {AlbumDto,BandClient,BandsMembersWikiDto,BandWikiDto} from "sharedLogic"
import {Navigate,useParams} from "react-router-dom"
import {Button,Heading,Link,Spinner,Tabs,ToggleButton,Tooltip} from "@heroui/react"
import {InformationCircleIcon,StarIcon as StarIconOutline} from "@heroicons/react/24/outline"
import {isMobile} from "react-device-detect"
import {TranslatedDescription} from "../TranslatedDescription/TranslatedDescription.tsx"
import {DataTable} from "../DataTable/DataTable.tsx"
import {StarIcon} from "@heroicons/react/24/solid"
import {toggleArtistFavoriteAll,toggleBandFavorite} from "../../helpers/toggleFavorite.tsx"

export function BandData({strings}: { strings: Record<string,string> }){
    const [band,setBand]=useState<BandWikiDto | null>(new BandWikiDto())
    const [isLoading,setIsLoading]=useState(true)
    const [showingBandMembers,setShowingBandMembers]=useState<readonly BandsMembersWikiDto[] | null>(null)
    const [showingAlbums,setShowingAlbums]=useState<readonly AlbumDto[] | null>(null)
    const translate=(key: string) => strings[key]??key
    const params=useParams()
    const bandIdParam=parseInt(params.band?params.band:"0")

    const memberFilterOptions=["All","Current","Past"]
    const albumFilterOptions=["All","Studio"]

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            if(bandIdParam==null) return
            const token=localStorage.getItem("token")
            const bandData=await BandClient.getInstance().getBandWikiPageDataById(bandIdParam,token)
            setIsLoading(false)
            if(bandData!=null){
                setBand(bandData)
                setShowingBandMembers(bandData.bandMembers!.asJsReadonlyArrayView())
                setShowingAlbums(bandData.albums!.asJsReadonlyArrayView())
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

    const bandGenres=band.computedGenres!.asJsReadonlyArrayView()

    const bandMembersList=band.bandMembers!.asJsReadonlyArrayView()
    const currentMembersList=bandMembersList.filter(member => member.yearRole!!.asJsReadonlyArrayView().some(yearRole => yearRole.includes("-)")))
    const pastMembersList=bandMembersList.filter(member => member.yearRole!!.asJsReadonlyArrayView().every(yearRole => !yearRole.includes("-)")))

    const albumsList=band.albums!.asJsReadonlyArrayView()
    const studioAlbumsList=albumsList.filter(album => album.type==="Studio")

    const similarBandsList=band.similar!.asJsReadonlyArrayView()
    const statusColor=band.status==="Active"?"text-success":band.status==="Disbanded"?"text-danger":"text-warning"

    const loggedIn=localStorage.getItem("token")!=null
    return (
        <div className={"flex flex-col gap-4 w-5xl mx-3"}>
            <div className={"flex gap-2"}>
                <Heading level={1}>{band.name}</Heading>
                <ToggleButton
                    isDisabled={!loggedIn}
                    variant={"ghost"}
                    isIconOnly
                    defaultSelected={band.favorite??false}
                    aria-label={translate("favorite")}
                    onPress={() => toggleBandFavorite(bandIdParam!)}
                    className={"bg-transparent! data-[selected=true]:bg-transparent! hover:bg-transparent!"}
                >
                    {({isSelected}) => (
                        isSelected?(
                            <StarIcon className={"size-8"}/>
                        ):(
                            <StarIconOutline className={"size-8"}/>
                        )
                    )}
                </ToggleButton>
            </div>
            <div className={"flex w-full min-h-32 gap-6"}>
                <div className={"flex flex-col"+(!isMobile?" w-9/12":"")}>
                    <div className={"flex min-h-32"}>
                        <div className={"w-5/9"}> {/*data column 1*/}
                            <p>
                                <span className={"text-muted"}>{translate("country")}: </span>
                                {translate(band.country!)}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("status")}: </span>
                                <span className={statusColor}>{translate(band.status!.toLowerCase())}</span>
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("years_active")}: </span>
                                {band.formedYear} - {band.disbandedYear??translate("present")}
                            </p>
                        </div>
                        <div className={"w-4/9"}>
                            <span className={"text-muted"}>{translate("top_genres")}: </span>
                            <Tooltip delay={0}>
                                <Tooltip.Trigger>
                                    <Button isIconOnly aria-label="Genre info" variant="ghost"
                                            className={"border-none shadow-none h-auto w-auto min-w-0 p-0 inline-flex align-text-bottom"}>
                                        <InformationCircleIcon/>
                                    </Button>
                                </Tooltip.Trigger>
                                <Tooltip.Content>
                                    <p>{translate("band_genre_info")}</p>
                                </Tooltip.Content>
                            </Tooltip>
                            {bandGenres.map((genre,index) =>
                                <p key={index}>-{genre.name} </p>
                            )}
                        </div>
                        {/*data column 2*/}
                    </div>

                    {isMobile&&<div className={"max-w-xl flex justify-center my-6"}> {/*mobile image*/}
                        <img src={band.imageUrl!} alt={band.name!} className={"rounded-md"}/>
                    </div>}

                    <TranslatedDescription
                        description={band.description??""}
                        translateDescription={() => {
                        }}
                    />
                </div>
                {!isMobile&&
                    <div className={"size-72"}>
                        <img src={band.imageUrl!} alt={band.name!} className={"rounded-md"}/>
                    </div>
                }
            </div>
            <div className={isMobile?"w-96":"w-full"}>
                <Tabs variant={"secondary"}>
                    <Tabs.ListContainer>
                        <Tabs.List aria-label={"data tables"}>
                            <Tabs.Tab id={"members"} className={"whitespace-nowrap"}>
                                {translate("members")}
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                            <Tabs.Tab id={"albums"} className={"whitespace-nowrap"}>
                                {translate("albums")}
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                            <Tabs.Tab id={"similar"} className={"whitespace-nowrap"}>
                                {translate("similar_bands")}
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                        </Tabs.List>
                    </Tabs.ListContainer>
                    <Tabs.Panel id={"members"}>
                        <div className={"flex flex-col gap-4"}>
                            <div className="flex items-center justify-between">
                                <div
                                    role="radiogroup"
                                    aria-label="select band member type"
                                    className="inline-flex h-fit items-center gap-1 rounded-full border bg-background-secondary"
                                >
                                    {memberFilterOptions.map((item) => {
                                        const isSelected=(item==="All"&&showingBandMembers?.every((member,index) => member===bandMembersList[index])&&showingBandMembers.length===bandMembersList.length)||
                                            (item==="Current"&&showingBandMembers?.every((member,index) => member===currentMembersList[index])&&showingBandMembers.length===currentMembersList.length)||
                                            (item==="Past"&&showingBandMembers?.every((member,index) => member===pastMembersList[index])&&showingBandMembers.length===pastMembersList.length)
                                        return (
                                            <Button
                                                key={item}
                                                aria-checked={isSelected}
                                                aria-label={item}
                                                variant="ghost"
                                                onPress={() => {
                                                    if(item==="All") setShowingBandMembers(bandMembersList)
                                                    else if(item==="Current") setShowingBandMembers(currentMembersList)
                                                    else if(item==="Past") setShowingBandMembers(pastMembersList)
                                                }}
                                                className={`h-8 w-fit text-md p-2 min-w-16 ${
                                                    isSelected
                                                        ?"bg-overlay! hover:bg-overlay!"
                                                        :"bg-secondary! hover:bg-secondary!"
                                                }`}
                                            >
                                                {translate(item.toLowerCase()+"_members")}
                                            </Button>
                                        )
                                    })}
                                </div>
                                <Button
                                    isDisabled={!loggedIn}
                                    variant={"secondary"}
                                    onPress={() => toggleArtistFavoriteAll(bandIdParam)}
                                >
                                    {translate("favorite_all")}
                                </Button>
                            </div>
                            <DataTable
                                columns={[
                                    {
                                        title: translate("person_name"),value: (
                                            item => <Link href={"../artist/"+item.artistId}>{item.artistName}</Link>
                                        )
                                    },
                                    {
                                        title: translate("role"),
                                        value: item => item.yearRole!!.asJsReadonlyArrayView().join('\n')
                                            .replace(/guitar/gi,translate("guitar"))
                                            .replace(/bass/gi,translate("bass"))
                                            .replace(/drums/gi,translate("drums"))
                                            .replace(/backing vocals/gi,translate("backing_vocals"))
                                            .replace(/vocals/gi,translate("vocals"))
                                    },
                                ]}
                                data={showingBandMembers!}
                                emptyText={translate("no_band_members")}
                            />
                        </div>
                    </Tabs.Panel>
                    <Tabs.Panel id={"albums"}>
                        <div className={"flex flex-col gap-4"}>
                            <div className="flex items-center">
                                <div
                                    role="radiogroup"
                                    aria-label="select album type"
                                    className="inline-flex h-fit items-center gap-1 rounded-full border bg-background-secondary"
                                >
                                    {albumFilterOptions.map((item) => {
                                        const isSelected=(item==="All"&&showingAlbums?.every((album,index) => album===albumsList[index])&&showingAlbums.length===albumsList.length)||
                                            (item==="Studio"&&showingAlbums?.every((album,index) => album===studioAlbumsList[index])&&showingAlbums.length===studioAlbumsList.length)
                                        return (
                                            <Button
                                                key={item}
                                                aria-checked={isSelected}
                                                aria-label={item}
                                                variant="ghost"
                                                onPress={() => {
                                                    if(item==="All") setShowingAlbums(albumsList)
                                                    else if(item==="Studio") setShowingAlbums(studioAlbumsList)
                                                }}
                                                className={`h-8 w-fit text-md p-2 min-w-16 ${
                                                    isSelected
                                                        ?"bg-overlay! hover:bg-overlay!"
                                                        :"bg-secondary! hover:bg-secondary!"
                                                }`}
                                            >
                                                {translate(item.toLowerCase())}
                                            </Button>
                                        )
                                    })}

                                </div>
                            </div>
                            <DataTable
                                columns={[
                                    {
                                        title: translate("title"),value: (
                                            item => <Link href={"../album/"+item.id}>{item.title}</Link>
                                        )
                                    },
                                    {title: translate("release_date"),value: item => item.releaseDate.toString()},
                                    {title: translate("album_type"),value: item => item.type},
                                    {title: translate("main_genre"),value: item => item.genreName},
                                ]}
                                data={showingAlbums!}
                                emptyText={translate("no_albums")}
                            />
                        </div>
                    </Tabs.Panel>
                    <Tabs.Panel id={"similar"}>
                        <DataTable
                            columns={[
                                {
                                    title: translate("thing_name"),value: (
                                        item => <Link href={"../band/"+item.id}>{item.name}</Link>
                                    )
                                },
                                {title: translate("formed_year"),value: item => item.formedYear},
                                {title: translate("country"),value: item => translate(item.country!)}
                            ]}
                            data={similarBandsList}
                            emptyText={translate("no_similar_bands")}
                        />

                    </Tabs.Panel>
                </Tabs>
            </div>

        </div>
    )
}