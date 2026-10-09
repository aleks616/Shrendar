import React,{useEffect,useState} from 'react'
import {Navigate,useParams} from "react-router-dom"
import {ProfileClient,UserProfileDto} from "sharedLogic"
import {
    Avatar,
    Badge,
    EmptyState,
    Label,
    Link as HerouiLink,
    ProgressBar,
    Spinner,
    Surface,
    Table,
    Tabs,
    ToggleButton,
    Tooltip
} from "@heroui/react"
import {InboxIcon,StarIcon,UserIcon} from '@heroicons/react/24/solid'
import {StarIcon as StarIconOutline} from '@heroicons/react/24/outline'
import {loremIpsum} from "lorem-ipsum"
import {isMobile} from "react-device-detect"
import {EditableSurface} from "../EditableSurface/EditableSurface.tsx"
import {DataTable} from "../DataTable/DataTable.tsx"

const BLUE_AVATAR_URL="https://heroui-assets.nyc3.cdn.digitaloceanspaces.com/avatars/bluwefwefe.jpg"

export function UserProfile({strings}: { strings: Record<string,string> }){
    const [user,setUser]=useState<UserProfileDto | null>(new UserProfileDto())
    const [isLoading,setIsLoading]=useState(true)
    const translate=(key: string) => strings[key]??key
    const params=useParams()
    const userParam=params.user
    useEffect(() => {
        const fetchUserData=async () => {
            if(userParam==null) return
            const token=localStorage.getItem("token")
            const userData=await ProfileClient.getInstance().getUserProfile(userParam,token??null)
            setIsLoading(false)
            if(userData!=null){
                setUser(userData)
            }
            else{
                setUser(null)
            }
        }
        fetchUserData()
    },[userParam])

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    if(user==null){
        return <Navigate to="/404" replace={false}/>
    }

    const ranks: number[]=[
        1,15,40,120,270,520,820,1200,1700,
        2400,3500,5500,8000,11000,16000,21000,
        182500,400000
    ]

    const calculateRankProgress=() => {
        if(user.rankId<1||user.rankId>17) return -1
        const nextMinXp=ranks[user.rankId]
        const currentMinXp=ranks[user.rankId-1]
        return (user.xp-currentMinXp)/(nextMinXp-currentMinXp)
    }

    const isOnline=false //todo actual logic
    const isOwnProfile=user.user

    const bandList=user.favoriteBands!.asJsReadonlyArrayView()
    const artistList=user.favoriteArtists!.asJsReadonlyArrayView()
    const genreList=user.favoriteGenres!.asJsReadonlyArrayView()
    const contributionList=user.contributions!.asJsReadonlyArrayView()

    const toggleBandFavorite=async (id: number) => {
        const token=localStorage.getItem("token")
        const result=await ProfileClient.getInstance().toggleFavoriteBand(id,token)
        if(result!="band_toggled") console.error(result)
    }

    const toggleArtistFavorite=async (id: bigint) => {
        const token=localStorage.getItem("token")
        const result=await ProfileClient.getInstance().toggleFavoriteArtist(id,token)
        if(result!="artist_toggled") console.error(result)
    }

    const toggleGenreFavorite=async (id: number) => {
        const token=localStorage.getItem("token")
        const result=await ProfileClient.getInstance().toggleFavoriteGenre(id,token)
        if(result!="genre_toggled") console.error(result)
    }

    //todo: move to shared
    const translateDate=(date: string) => {
        if(date==="today") return translate("today")
        const dateParts=date.split(" ")
        const unit=dateParts[1]
        const amount=parseInt(dateParts[0])
        return `${amount} ${translate(unit)} ${translate("time_ago")}`
    }

    const updateBio=async (newBio: string) => {
        const token=localStorage.getItem("token")
        const result=await ProfileClient.getInstance().updateBio(newBio,token)
        if(result!="bio_added") console.log(result)
    }

    const mobile=isMobile

    return (
        <div className={"flex flex-col gap-4 max-w-4xl mx-3"}>
            <div className={"flex gap-3"}>
                <div>
                    <Badge.Anchor>
                        <Avatar size={"lg"} color="accent" variant="soft" className={"size-20"}>
                            <Avatar.Image src={BLUE_AVATAR_URL}/>
                            <Avatar.Fallback>
                                <UserIcon className={"size-12"}/>
                            </Avatar.Fallback>
                        </Avatar>
                        <Tooltip delay={5}>
                            <Tooltip.Trigger>
                                <Badge color={isOnline?"success":"default"} placement="bottom-right" size="md"/>
                            </Tooltip.Trigger>
                            <Tooltip.Content showArrow>
                                <p>{translate("last_online")} {translateDate(user.lastLogin!)}</p>
                            </Tooltip.Content>
                        </Tooltip>
                    </Badge.Anchor>
                </div>
                <div>
                    <p className={"text-2xl font-bold"}>{user.username}</p>
                    <p className={"text-lg text-muted"}>@{user.login}</p>
                    <p className={"text-sm"}>{translate("member_since")} {translateDate(user.accountAge!)}</p>
                </div>
            </div>
            {calculateRankProgress()!= -1&&
                <ProgressBar value={calculateRankProgress()*100}>
                    <Label>
                        <div>
                            {translate("level")} {user.rankId} <br/>{translate("rank"+user.rankId)}
                        </div>
                    </Label>
                    <ProgressBar.Output>
                        {translate("level")} {user.rankId+1} <br/>{translate("rank"+(user.rankId+1))}
                    </ProgressBar.Output>
                    <ProgressBar.Track>
                        <ProgressBar.Fill/>
                    </ProgressBar.Track>
                </ProgressBar>}
            <div className={"md:min-w-2xl"}>
                <p>{translate("bio")}</p>
                {user.user?(
                    <EditableSurface
                        value={user.bio??""}
                        onSave={updateBio}
                    />
                ):(
                    <Surface className={"h-52 w-full border-accent rounded-xl p-2 overflow-y-auto"}
                             variant={"secondary"}>
                        {user.bio??loremIpsum({count: 8,units: "sentences"})}
                    </Surface>
                )}
            </div>
            <div className={mobile?"w-96":"w-full"}>
                <Tabs variant={"secondary"}>
                    <Tabs.ListContainer>
                        <Tabs.List aria-label="Options">
                            <Tabs.Tab id="favorite_bands" className={"whitespace-nowrap"}>
                                {translate("favorite_bands")}
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                            <Tabs.Tab id="favorite_artists" className={"whitespace-nowrap"}>
                                {translate("favorite_artists")}
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                            <Tabs.Tab id="favorite_genres" className={"whitespace-nowrap"}>
                                {translate("favorite_genres")}
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                            <Tabs.Tab id="contributions" className={"whitespace-nowrap"}>
                                {translate("contributions")}
                                <Tabs.Separator/>
                                <Tabs.Indicator/>
                            </Tabs.Tab>
                        </Tabs.List>
                    </Tabs.ListContainer>
                    <Tabs.Panel className="pt-2" id="favorite_bands">
                        <Table variant={"secondary"}>
                            <Table.ScrollContainer className={"max-h-96 overflow-y-auto"}>
                                <Table.Content aria-label={translate("favorite_bands_table")} className={"w-full"}>
                                    <Table.Header className={"sticky top-0 z-10"}>
                                        {isOwnProfile&&
                                            <Table.Column isRowHeader className={"w-16"}>
                                                {translate("toggle")}
                                            </Table.Column>}
                                        <Table.Column isRowHeader>{translate("band_name")}</Table.Column>
                                        <Table.Column>{translate("country")}</Table.Column>
                                        <Table.Column>{translate("active")}</Table.Column>
                                    </Table.Header>
                                    <Table.Body
                                        renderEmptyState={() => (
                                            <EmptyState
                                                className="flex h-full flex-col items-center justify-center gap-4 text-center">
                                                <InboxIcon className="size-6 text-muted"/>
                                                <span
                                                    className="text-sm text-muted">{translate("no_favorite_bands")}</span>
                                            </EmptyState>
                                        )}
                                    >
                                        {bandList.map((item,i) => (
                                            <Table.Row key={i}>
                                                {isOwnProfile&&
                                                    <Table.Cell>
                                                        <div className={"flex justify-center"}>
                                                            <ToggleButton
                                                                variant={"ghost"}
                                                                defaultSelected
                                                                isIconOnly
                                                                aria-label={translate("favorite")}
                                                                onPress={() => toggleBandFavorite(item.id!)}
                                                                className={"bg-transparent! data-[selected=true]:bg-transparent! hover:bg-transparent!"}
                                                            >
                                                                {({isSelected}) => (
                                                                    isSelected?(
                                                                        <StarIcon className={"size-6"}/>
                                                                    ):(
                                                                        <StarIconOutline className={"size-6"}/>
                                                                    )
                                                                )}
                                                            </ToggleButton>
                                                        </div>
                                                    </Table.Cell>
                                                }
                                                <Table.Cell>
                                                    <HerouiLink
                                                        href={`../band/${item.id}`}
                                                        className="before:absolute before:inset-0"
                                                    >
                                                        {item.name}
                                                    </HerouiLink>
                                                </Table.Cell>
                                                <Table.Cell>{translate(item.country!)}</Table.Cell>
                                                <Table.Cell>{item.activeYears}</Table.Cell>
                                            </Table.Row>
                                        ))}
                                    </Table.Body>
                                </Table.Content>
                            </Table.ScrollContainer>
                        </Table>
                    </Tabs.Panel>
                    <Tabs.Panel className="pt-2" id="favorite_artists">
                        <Table variant={"secondary"}>
                            <Table.ScrollContainer className={"max-h-96 overflow-y-auto"}>
                                <Table.Content aria-label={translate("favorite_artists_table")} className={"w-full"}>
                                    <Table.Header className={"sticky top-0 z-10"}>
                                        {isOwnProfile&&
                                            <Table.Column isRowHeader
                                                          className={"w-16"}>{translate("toggle")}</Table.Column>}
                                        <Table.Column isRowHeader>{translate("artist_name")}</Table.Column>
                                        <Table.Column>{translate("bands")}</Table.Column>
                                    </Table.Header>
                                    <Table.Body
                                        renderEmptyState={() => (
                                            <EmptyState
                                                className="flex h-full flex-col items-center justify-center gap-4 text-center">
                                                <InboxIcon className="size-6 text-muted"/>
                                                <span
                                                    className="text-sm text-muted">{translate("no_favorite_artists")}</span>
                                            </EmptyState>
                                        )}
                                    >
                                        {artistList.map((item,i) => {
                                            const bands=item.bands!.asJsReadonlyArrayView()
                                            const currentBands: typeof bands[number][]=[]
                                            const pastBands: typeof bands[number][]=[]

                                            bands.forEach(band => {
                                                if(band.current===true) currentBands.push(band)
                                                else if(band.current===false) pastBands.push(band)
                                            })

                                            return (
                                                <Table.Row key={i}>
                                                    {isOwnProfile&&
                                                        <Table.Cell>
                                                            <div className={"flex justify-center"}>
                                                                <ToggleButton
                                                                    variant={"ghost"}
                                                                    defaultSelected
                                                                    isIconOnly
                                                                    aria-label={translate("favorite")}
                                                                    onPress={() => toggleArtistFavorite(item.id!)}
                                                                    className={"bg-transparent! data-[selected=true]:bg-transparent! hover:bg-transparent!"}
                                                                >
                                                                    {({isSelected}) => (
                                                                        isSelected?(
                                                                            <StarIcon className={"size-6"}/>
                                                                        ):(
                                                                            <StarIconOutline className={"size-6"}/>
                                                                        )
                                                                    )}
                                                                </ToggleButton>
                                                            </div>
                                                        </Table.Cell>
                                                    }
                                                    <Table.Cell>
                                                        <HerouiLink
                                                            href={`../artist/${item.id}`}
                                                            className="before:absolute before:inset-0"
                                                        >
                                                            {item.name}
                                                        </HerouiLink>
                                                    </Table.Cell>
                                                    <Table.Cell>
                                                        {currentBands.map((band,j) => (
                                                            <HerouiLink
                                                                key={j}
                                                                href={`../band/${band.bandId}`}
                                                                className="text-inherit before:absolute before:inset-0"
                                                            >
                                                                {band.bandName}
                                                            </HerouiLink>
                                                        ))}
                                                        {currentBands.length>0&&<br/>}
                                                        {pastBands.length>0&&(
                                                            <span className={"text-muted"}>
                                                                    <span>{translate("past")}: </span>
                                                                {pastBands.map((band,j) => (
                                                                    <HerouiLink
                                                                        key={j}
                                                                        href={`../band/${band.bandId}`}
                                                                        className="text-inherit before:absolute before:inset-0"
                                                                    >
                                                                        {band.bandName}
                                                                    </HerouiLink>
                                                                ))}
                                                                </span>
                                                        )}
                                                    </Table.Cell>
                                                </Table.Row>
                                            )
                                        })}
                                    </Table.Body>
                                </Table.Content>
                            </Table.ScrollContainer>
                        </Table>
                    </Tabs.Panel>
                    <Tabs.Panel className="pt-2" id="favorite_genres">
                        <Table variant={"secondary"}>
                            <Table.ScrollContainer className={"max-h-96 overflow-y-auto"}>
                                <Table.Content aria-label={translate("favorite_genres_table")} className={"w-full"}>
                                    <Table.Header className={"sticky top-0 z-10"}>
                                        {isOwnProfile&&
                                            <Table.Column isRowHeader
                                                          className={"w-16"}>{translate("toggle")}</Table.Column>}
                                        <Table.Column isRowHeader>{translate("genre")}</Table.Column>
                                    </Table.Header>
                                    <Table.Body
                                        renderEmptyState={() => (
                                            <EmptyState
                                                className="flex h-full flex-col items-center justify-center gap-4 text-center">
                                                <InboxIcon className="size-6 text-muted"/>
                                                <span
                                                    className="text-sm text-muted">{translate("no_favorite_genres")}</span>
                                            </EmptyState>
                                        )}
                                    >
                                        {genreList.map((item,i) => (
                                            <Table.Row key={i}>
                                                {isOwnProfile&&
                                                    <Table.Cell>
                                                        <div className={"flex justify-center"}>
                                                            <ToggleButton
                                                                variant={"ghost"}
                                                                defaultSelected
                                                                isIconOnly
                                                                aria-label={translate("favorite")}
                                                                onPress={() => toggleGenreFavorite(item.id!)}
                                                                className={"bg-transparent! data-[selected=true]:bg-transparent! hover:bg-transparent!"}
                                                            >
                                                                {({isSelected}) => (
                                                                    isSelected?(
                                                                        <StarIcon className={"size-6"}/>
                                                                    ):(
                                                                        <StarIconOutline className={"size-6"}/>
                                                                    )
                                                                )}
                                                            </ToggleButton>
                                                        </div>
                                                    </Table.Cell>
                                                }
                                                <Table.Cell>{item.name}</Table.Cell>
                                            </Table.Row>
                                        ))}
                                    </Table.Body>
                                </Table.Content>
                            </Table.ScrollContainer>
                        </Table>
                    </Tabs.Panel>
                    <Tabs.Panel className="pt-2" id="contributions">
                        <DataTable
                            columns={[
                                {title: translate("action"),value: item => item.action},
                                {title: translate("date"),value: item => item.changedAt},
                                {title: translate("table"),value: item => item.changedTable},
                                {title: translate("column"),value: item => item.changedColumn},
                                {title: translate("confirmed"),value: item => item.confirmed==true?"✔️":"️✖️"},
                                {title: translate("before"),value: item => item.oldValue?item.oldValue:"-"},
                                {title: translate("after"),value: item => item.newValue}
                            ]}
                            data={contributionList}
                            emptyText={translate("no_contributions")}
                        />
                    </Tabs.Panel>
                </Tabs>
            </div>
        </div>
    )
}
