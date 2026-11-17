import {Button,DateValue,Form,Heading,Input,Label,TextArea,TextField} from "@heroui/react"
import React,{useState} from "react"
import {AlbumAddDto,AlbumClient,Nullable} from "sharedLogic"
import {CalendarDate} from "@internationalized/date"
import {Navigate} from "react-router-dom"
import {AlbumImportancePicker} from "../AlbumImportancePicker/AlbumImportancePicker.tsx"
import {AlbumTypePicker} from "../AlbumTypePicker/AlbumTypePicker.tsx"
import {CalendarDateField} from "../CalendarDateField/CalendarDateField.tsx"
import {BandPicker} from "../BandPicker/BandPicker.tsx"
import {GenrePicker} from "../GenrePicker/GenrePicker.tsx"
import {ImageField} from "../ImageField/ImageField.tsx"

export function AddAlbumForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [title,setTitle]=useState("")
    const [releaseDate,setReleaseDate]=useState<DateValue | null>(null)
    const [albumType,setAlbumType]=useState<string | null>(null)
    const [description,setDescription]=useState("")
    const [bandId,setBandId]=useState<number | null>(null)
    const [bandFormedYear,setBandFormedYear]=useState<Nullable<number>>(null)
    const [genre,setGenre]=useState<string | null>(null)
    const [importance,setImportance]=useState<number>(0)
    const [imageUrl,setImageUrl]=useState("")
    const [resultText,setResultText]=useState("")

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }

    const minReleaseDate=() => {
        if(bandFormedYear!=null)
            return new CalendarDate(bandFormedYear,1,1)
        else return null
    }
    const today=new Date()
    const maxReleaseDate=new CalendarDate(today.getFullYear()+1,today.getMonth()+1,today.getDate())

    const submit=async () => {
        if(title!=null&&bandId!=null&&releaseDate!=null&&albumType!=null&&token!=null&&genre!=null){
            const date=releaseDate as CalendarDate
            const album=new AlbumAddDto(
                null,
                bandId,
                title,
                date.toString(),
                albumType.toUpperCase(),
                description,
                parseInt(genre),
                importance,
                imageUrl??null
            )
            const result=await AlbumClient.getInstance().addAlbum(album,token)
            setResultText(result)
        }
    }

    return (
        <div className={"w-9/12 flex flex-col gap-4"}>
            <Heading level={1} className={"text-center"}>{translate("enter_album_data")}</Heading>
            <Form>
                <div className={"w-full flex flex-col lg:flex-row justify-around"}>
                    <section className={"flex flex-col gap-4 min-w-md"}>
                        <BandPicker
                            bandId={bandId}
                            onChange={band => {
                                setBandId(band.id)
                                setBandFormedYear(band.formedYear)
                            }}
                            strings={strings}
                        />

                        <TextField isRequired name={"title"} type={"text"} value={title} onChange={setTitle}>
                            <Label>{translate("album_title")}</Label>
                            <Input/>
                        </TextField>

                        <CalendarDateField
                            isDisabled={bandId==null}
                            minValue={minReleaseDate()}
                            maxValue={maxReleaseDate}
                            value={releaseDate}
                            onChange={setReleaseDate}
                            label={translate("release_date")}
                            fieldError={translate("invalid_date")}
                            description={translate("choose_band_first")}
                        />

                        <AlbumTypePicker
                            albumType={albumType}
                            importance={importance}
                            onAlbumTypeChange={setAlbumType}
                            onImportanceChange={setImportance}
                            strings={strings}
                        />

                        <AlbumImportancePicker
                            albumType={albumType}
                            importance={importance}
                            onChange={setImportance}
                            strings={strings}
                        />

                        <div className={"flex flex-col gap-1"}>
                            <Label>{translate("description")}</Label>
                            <TextArea
                                className={"min-w-md min-h-32"}
                                value={description}
                                placeholder={translate("enter_album_description")}
                                onChange={e => setDescription(e.target.value)}
                                aria-label="album description"
                            />
                        </div>
                    </section>
                    <section className={"flex flex-col gap-4 min-w-md"}>
                        <GenrePicker genre={genre} onChange={setGenre} strings={strings}/>

                        <ImageField
                            imageUrl={imageUrl}
                            onChange={setImageUrl}
                            placeholder={translate("enter_image_url")}
                            description={translate("album_artwork_url_info")}
                            strings={strings}
                        />
                    </section>
                </div>
                <div className={"my-4 lg:flex items-center justify-center"}>
                    <Button className={"min-w-48"} onPress={submit}>{translate("add")}</Button>
                </div>
                <p>{translate(resultText)}</p>
            </Form>
        </div>
    )
}