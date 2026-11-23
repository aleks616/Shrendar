import React,{useEffect,useState} from "react"
import {Navigate,useParams} from "react-router-dom"
import {Button,ErrorMessage,FieldError,Form,Heading,Input,Label,Link,NumberField,TextField} from "@heroui/react"
import {ArtistBandAddDto,ArtistMemberAddDto,BandClient,BandWikiDto} from "sharedLogic"
import type {KtList} from "sharedLogic"
// @ts-ignore
import {KtList as KtListRuntime} from "sharedLogic/kotlin-kotlin-stdlib.mjs"
import {ArtistPicker} from "../ArtistPicker/ArtistPicker.tsx"
import {DataTable} from "../DataTable/DataTable.tsx"
import {TrashIcon} from "@heroicons/react/24/solid"

interface MemberPreview {
    artistId: bigint
    name: string
    nickname: string
    joinedYear: number
    leftYear: number | undefined
    role: string
}

export function AddBandMembersForm({strings}: { strings: Record<string,string> }){
    const [band,setBand]=useState<BandWikiDto | null>(null)
    const [artist,setArtist]=useState<ArtistMemberAddDto | null>(null)
    const [nickname,setNickname]=useState("")
    const [joinedYear,setJoinedYear]=useState<number | undefined>(undefined)
    const [leftYear,setLeftYear]=useState<number | undefined>(undefined)
    const [role,setRole]=useState("")
    const [members,setMembers]=useState<MemberPreview[]>([])
    const [isDuplicate,setIsDuplicate]=useState(false)
    const [resultText,setResultText]=useState("")
    const translate=(key: string) => strings[key]??key
    const currentYear=new Date().getFullYear()
    const params=useParams()
    const bandIdParam=parseInt(params.band?params.band:"0")

    useEffect(() => {
        const fetchData=async () => {
            const token=localStorage.getItem("token")
            const bandData=await BandClient.getInstance().getBandWikiPageDataById(bandIdParam,token)
            setBand(bandData)
        }
        fetchData()
    },[bandIdParam])

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }

    const getJoinedYearError=(): string | null => {
        if(joinedYear==undefined){
            return null
        }
        if(joinedYear>currentYear){
            return "joined_future"
        }
        if(band?.formedYear!=null&&joinedYear<band.formedYear){
            return "Band can't be joined before it was formed"
        }
        if(band?.disbandedYear!=null&&joinedYear>band.disbandedYear){
            return "Band can't be joined after it was disbanded"
        }
        if(artist?.birthYear!=null&&joinedYear-artist.birthYear<10){
            return "artist_too_young_joining"
        }
        if(artist?.deathYear!=null&&joinedYear>artist.deathYear){
            return "artist_dead_joining"
        }
        return null
    }

    const getLeftYearError=(): string | null => {
        if(leftYear==undefined){
            return null
        }
        if(leftYear>currentYear){
            return "left_future"
        }
        if(joinedYear!=undefined&&leftYear<joinedYear){
            return "left_before_joined"
        }
        if(artist?.deathYear!=null&&leftYear>artist.deathYear){
            return "artist_leave_when_dead"
        }
        return null
    }

    const isDuplicateMember=(artistId: bigint,joinedYear: number,leftYear: number | undefined,role: string): boolean => {
        return members.some(member =>
            member.artistId===artistId&&
            member.role===role&&
            (member.joinedYear===joinedYear||member.leftYear===leftYear)
        )
    }

    const addMember=() => {
        if(artist==null||joinedYear==undefined||getJoinedYearError()!=null||getLeftYearError()!=null||role.trim()===""||role.length>20){
            return
        }
        if(isDuplicateMember(artist.id,joinedYear,leftYear,role)){
            setIsDuplicate(true)
            return
        }
        setIsDuplicate(false)
        setMembers([...members,{artistId: artist.id,name: artist.name??"",nickname,joinedYear,leftYear,role}])
    }

    const removeMember=(member: MemberPreview) => {
        setMembers(members.filter(item => item!==member))
    }

    const submit=async () => {
        if(token!=null){
            const data:KtList<ArtistBandAddDto>=KtListRuntime.fromJsArray(members.map(m => new ArtistBandAddDto(
                null,
                bandIdParam,
                m.artistId,
                m.nickname,
                m.role,
                m.joinedYear,
                m.leftYear,
            )))
            const result=await BandClient.getInstance().addBandMembers(data,token)
            setResultText(result)
        }
    }


    return (
        <div className={"w-9/12 flex flex-col gap-4"}>
            <Heading level={1} className={"text-center"}>{translate("add_members")}</Heading>
            <div className={"text-center"}>
                <p className={"text-lg"}>{translate("adding_to")} {band?.name} ({translate(band?.country!)})</p>
            </div>
            <Form>
                <div className={"w-full flex justify-center"}>
                    <section className={"flex flex-col gap-4 min-w-lg max-w-2xl"}>
                        <ArtistPicker
                            artistId={artist?.id??null}
                            onChange={setArtist}
                            strings={strings}
                        />
                        <TextField
                            name={"nickname"}
                            type={"text"}
                            value={nickname}
                            onChange={setNickname}
                        >
                            <Label>{translate("nickname")}</Label>
                            <Input placeholder={translate("nickname")}/>
                        </TextField>
                        <Button variant={"secondary"}>
                            <Link href={"/artist-add"} target={"_blank"}>{translate("missing_artist")}</Link>
                        </Button>

                        <NumberField
                            formatOptions={{useGrouping: false}}
                            isRequired
                            value={joinedYear}
                            onChange={setJoinedYear}
                            isInvalid={getJoinedYearError()!=null}
                        >
                            <Label>{translate("joined_year")}</Label>
                            <NumberField.Group>
                                <NumberField.DecrementButton/>
                                <NumberField.Input placeholder={"1983"}/>
                                <NumberField.IncrementButton/>
                            </NumberField.Group>
                            <FieldError>{translate(getJoinedYearError()??"")}</FieldError>
                        </NumberField>

                        <NumberField
                            formatOptions={{useGrouping: false}}
                            value={leftYear}
                            onChange={setLeftYear}
                            isInvalid={getLeftYearError()!=null}
                        >
                            <Label>{translate("left_year")}</Label>
                            <NumberField.Group>
                                <NumberField.DecrementButton/>
                                <NumberField.Input placeholder={"1990"}/>
                                <NumberField.IncrementButton/>
                            </NumberField.Group>
                            <FieldError>{translate(getLeftYearError()??"")}</FieldError>
                        </NumberField>

                        <TextField
                            name={"role"}
                            type={"text"}
                            value={role}
                            onChange={setRole}
                            isRequired
                            isInvalid={role.length>20}
                        >
                            <Label>{translate("role")}</Label>
                            <Input placeholder={translate("role")}/>
                            <FieldError>{translate("role_too_long")}</FieldError>
                        </TextField>
                        <Button onPress={addMember} variant={"secondary"} className={"my-2"}>
                            {translate("add_to_preview")}
                        </Button>
                        {isDuplicate&&<ErrorMessage>duplicate error, entry not added</ErrorMessage>}

                        {members.length>0&&(
                            <DataTable
                                columns={[
                                    {title: "name",value: item => item.name},
                                    {title: "nickname",value: item => item.nickname},
                                    {title: "joined",value: item => item.joinedYear},
                                    {title: "left",value: item => item.leftYear},
                                    {title: "role",value: item => item.role},
                                    {
                                        title: "",value: item => (
                                            <Button
                                                isIconOnly
                                                variant={"ghost"}
                                                aria-label="Delete"
                                                onPress={() => removeMember(item)}
                                            >
                                                <TrashIcon className="h-5 w-5"/>
                                            </Button>
                                        )
                                    },
                                ]}
                                data={members}
                                emptyText={translate("no_results")
                                }
                            />
                        )}

                        <div className={"my-4 lg:flex items-center justify-center"}>
                            <Button className={"min-w-48"} isDisabled={members.length<1} onPress={submit}>
                                {translate("add_all")}
                            </Button>
                        </div>
                        <p>{translate(resultText)}</p>
                    </section>
                </div>
            </Form>
        </div>
    )


}