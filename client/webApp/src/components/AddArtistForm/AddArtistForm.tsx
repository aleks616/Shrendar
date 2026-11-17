import {Button,Checkbox,DateValue,Form,Heading,Input,Label,TextArea,TextField} from "@heroui/react"
import React,{useState} from "react"
import {ArtistAddDto,ArtistClient} from "sharedLogic"
import {Navigate} from "react-router-dom"
import {CalendarDate} from "@internationalized/date"
import {CalendarDateField} from "../CalendarDateField/CalendarDateField.tsx"
import {CountryPicker} from "../CountryPicker/CountryPicker.tsx"
import {GenderPicker} from "../GenderPicker/GenderPicker.tsx"
import {ImageField} from "../ImageField/ImageField.tsx"

export function AddArtistForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [name,setName]=useState("")
    const [birthdate,setBirthdate]=useState<DateValue | null>(null)
    const [isDead,setIsDead]=useState(false)
    const [deathdate,setDeathdate]=useState<DateValue | null>(null)
    const [gender,setGender]=useState<string>("M")

    const [countryId,setCountryId]=useState<number | null>(null)
    const [description,setDescription]=useState("")
    const [imageUrl,setImageUrl]=useState("")
    const [resultText,setResultText]=useState("")

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }
    const today=new Date()
    const maxBirthDate=new CalendarDate(today.getFullYear()-10,today.getMonth(),today.getDate())
    const minDeathDate=birthdate!=null?new CalendarDate(birthdate.year+10,birthdate.month,birthdate.day):null
    const maxDeathDate=new CalendarDate(today.getFullYear(),today.getMonth(),today.getDate())

    const submit=async () => {
        if(name!=null&&birthdate!=null&&gender!=null&&token!=null&&(!isDead||(isDead&&deathdate!=null))){
            const newbirthdate=birthdate as CalendarDate
            const newdeathdate=deathdate as CalendarDate
            const artist=new ArtistAddDto(
                null,
                name,
                newbirthdate.toString(),
                newdeathdate!=null?newdeathdate.toString():null,
                gender,
                countryId,
                description,
                imageUrl
            )
            const result=await ArtistClient.getInstance().addArtist(artist,token)
            setResultText(result)
        }
    }

    return (
        <div className={"w-9/12 flex flex-col gap-4"}>
            <Heading level={1} className={"text-center"}>{translate("enter_artist_data")}</Heading>
            <Form>
                <div className={"w-full flex flex-col lg:flex-row justify-around"}>
                    <section className={"flex flex-col gap-4 min-w-md"}>
                        <TextField isRequired name={"name"} type={"text"} value={name} onChange={setName}>
                            <Label>{translate("artist_name")}</Label>
                            <Input/>
                        </TextField>

                        <CalendarDateField
                            isDisabled={false}
                            minValue={null}
                            maxValue={maxBirthDate}
                            value={birthdate}
                            onChange={setBirthdate}
                            label={translate("birthdate")}
                            fieldError={translate("invalid_artist_birthdate")}
                            description={null}
                        />

                        <Checkbox id={"is-dead"} isSelected={isDead} onChange={setIsDead}>
                            <Checkbox.Content>
                                <Checkbox.Control>
                                    <Checkbox.Indicator/>
                                </Checkbox.Control>
                                {translate("is_dead")}
                            </Checkbox.Content>
                        </Checkbox>

                        {isDead&&(
                            <CalendarDateField
                                isInvalid={(deathdate==null&&isDead)||(deathdate!=null&&birthdate!=null&&deathdate<birthdate)||(deathdate!=null&&deathdate>maxDeathDate)}
                                isDisabled={false}
                                minValue={minDeathDate}
                                maxValue={maxDeathDate}
                                value={deathdate}
                                onChange={setDeathdate}
                                label={translate("death_date")}
                                fieldError={translate("invalid_death_date")}
                                description={null}
                            />

                        )}

                        <GenderPicker gender={gender} onChange={setGender} strings={strings}/>

                        <div className={"flex flex-col gap-1"}>
                            <Label>{translate("description")}</Label>
                            <TextArea
                                className={"min-w-md min-h-32"}
                                value={description}
                                placeholder={translate("artist_description")}
                                onChange={e => setDescription(e.target.value)}
                                aria-label={translate("artist_description")}
                            />
                        </div>

                    </section>
                    <section className={"flex flex-col gap-4 min-w-md"}>

                        <CountryPicker countryId={countryId} onChange={setCountryId} strings={strings}/>

                        <ImageField
                            imageUrl={imageUrl}
                            onChange={setImageUrl}
                            placeholder={translate("enter_photo_url")}
                            description={translate("artist_photo_url")}
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