import React,{useEffect,useState} from "react"
import {ArtistClient,ArtistWikiDto} from "sharedLogic"
import {Navigate,useParams} from "react-router-dom"
import {Heading,Link,Spinner,ToggleButton} from "@heroui/react"
import {toggleArtistFavorite} from "../../helpers/toggleFavorite.tsx"
import {StarIcon} from "@heroicons/react/24/solid"
import {StarIcon as StarIconOutline} from "@heroicons/react/24/outline"
import {isMobile} from "react-device-detect"
import {TranslatedDescription} from "../TranslatedDescription/TranslatedDescription.tsx"
import {DataTable} from "../DataTable/DataTable.tsx"

export function ArtistData({strings}: { strings: Record<string,string> }){
    const [artist,setArtist]=useState<ArtistWikiDto | null>(new ArtistWikiDto())
    const [isLoading,setIsLoading]=useState(true)

    const translate=(key: string) => strings[key]??key
    const params=useParams()
    const artistIdParam=BigInt(params.artist?params.artist:"0")

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            if(artistIdParam==null) return
            const token=localStorage.getItem("token")
            const artistData=await ArtistClient.getInstance().getArtistWikiPageDataById(artistIdParam,token)
            if(artistData!=null){
                setArtist(artistData)
            }
            else{
                setArtist(null)
            }
            setIsLoading(false)
        }
        fetchData()
    },[artistIdParam])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(artist==null){
        return <Navigate to="/404"/>
    }

    const bands=artist.bands!.asJsReadonlyArrayView()
    const isDead=artist.deathDate!=null

    const loggedIn=localStorage.getItem("token")!=null
    return (
        <div className={"flex flex-col gap-4 w-5xl mx-3"}>
            <header className={"flex gap-2"}> {/*header*/}
                <Heading level={1}>{artist.name}</Heading>
                <ToggleButton
                    isDisabled={!loggedIn}
                    variant={"ghost"}
                    isIconOnly
                    defaultSelected={artist.favorite??false}
                    aria-label={translate("favorite")}
                    onPress={() => toggleArtistFavorite(artistIdParam)}
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
            </header>
            <main className={"flex w-full min-h-32 gap-6"}>
                <div className={"flex flex-col"+(!isMobile?" w-9/12":"")}>
                    <section className={"flex min-h-28"}>
                        <div className={"w-5/9"}>
                            <p>
                                <span className={"text-muted"}>{translate("country")}: </span>
                                {artist.country?translate(artist.country):"-"}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("age")}: </span>
                                {artist.age?artist.age:"-"}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("birthday")}: </span>
                                {artist.birthDate?artist.birthDate.toString():"-"}
                                <span className={"text-muted"}> {translate("next_in")}</span>
                                {artist.daysTillBirthday?artist.daysTillBirthday:"-"}
                                <span> {translate("days")}</span>
                            </p>
                            {isDead&&<p>
                                <span className={"text-muted"}>{translate("death_anniversary")}: </span>
                                {artist.deathDate.toString()}
                                <span className={"text-muted"}> {translate("next_in")}</span>
                                {artist.daysTillDeathAnniversary}
                                <span> {translate("days")}</span>
                            </p>}
                        </div>
                        <div className={"w-4/9"}>
                            <p>
                                <span className={"text-muted"}>{translate("gender")}: </span>
                                {translate(artist.gender?.toLowerCase()??"unknown")}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("zodiac_sign")}: </span>
                                {artist.zodiacSign?translate(`zodiac_${artist.zodiacSign.toString().toLowerCase()}`):"-"}
                            </p>
                            <p>
                                <span className={"text-muted"}>{translate("chinese_zodiac_sign")}: </span>
                                {artist.chineseZodiacSign?translate(`chinese_zodiac_${artist.chineseZodiacSign.toString().toLowerCase()}`):"-"}
                            </p>
                        </div>
                    </section>
                    {isMobile&&
                        <div className={"size-72"}>
                            <img src={artist.artistImageUrl!} alt={artist.name!} className={"rounded-md"}/>
                        </div>
                    }
                    <TranslatedDescription
                        description={artist.description??""}
                        translateDescription={() => {
                        }}
                    />
                </div>
                {!isMobile&&
                    <div className={"size-72"}>
                        <img src={artist.artistImageUrl!} alt={artist.name!} className={"rounded-md"}/>
                    </div>
                }
            </main>
            <div className={"flex flex-col gap-4"+(isMobile?" w-96":" w-full")}>
                <Heading level={3}>{translate("bands")}</Heading>
                <DataTable
                    columns={[{
                        title: translate("bands"),
                        value: (
                            item => <Link href={`../band/${item.bandId}`}>{item.bandName}</Link>
                        )
                    },
                        {
                            title: translate("role"),
                            value: item => (
                                item.yearRole!!.asJsReadonlyArrayView().map((yearRole) =>
                                    <span>
                                        {yearRole.replace(/guitar/gi,translate("guitar"))
                                            .replace(/bass/gi,translate("bass"))
                                            .replace(/drums/gi,translate("drums"))
                                            .replace(/backing vocals/gi,translate("backing_vocals"))
                                            .replace(/vocals/gi,translate("vocals"))} <br/>
                                    </span>)
                            )
                        },
                    ]}
                    data={bands}
                    emptyText={translate("no_bands")}
                />
            </div>
        </div>
    )
}