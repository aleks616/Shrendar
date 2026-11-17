import {Button,DateValue,Form,Heading,Input,Label,TextArea,TextField} from "@heroui/react"
import React,{useState} from "react"
import {EventAddDto,EventClient,Nullable} from "sharedLogic"
import {CalendarDate} from "@internationalized/date"
import {Navigate} from "react-router-dom"
import {BandPicker} from "../BandPicker/BandPicker.tsx"
import {CalendarDateField} from "../CalendarDateField/CalendarDateField.tsx"

export function AddEventForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [bandId,setBandId]=useState<number | null>(null)
    const [bandFormedYear,setBandFormedYear]=useState<Nullable<number>>(null)
    const [date,setDate]=useState<DateValue | null>(null)
    const [name,setName]=useState<string>("")
    const [description,setDescription]=useState<string>("")
    const [resultText,setResultText]=useState<string>("")

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }

    const minDate=() => {
        if(bandFormedYear!=null)
            return new CalendarDate(bandFormedYear,1,1)
        else return null
    }

    const today=new Date()
    const maxDate=new CalendarDate(today.getFullYear()+1,today.getMonth()+1,today.getDate())

    const submit=async () => {
        if(bandId!=null&&date!=null&&name!=""&&description!=""){
            const eventDate=date as CalendarDate
            const event=new EventAddDto(
                null,
                bandId,
                eventDate.toString(),
                name,
                description
            )
            const result=await EventClient.getInstance().addEvent(event,token)
            setResultText(result)
        }
    }

    return (
        <div className={"w-9/12 flex flex-col gap-4"}>
            <Form>
                <Heading level={1} className={"text-center"}>{translate("enter_event_data")}</Heading>
                <div className={"w-full flex justify-center"}>
                    <section className={"flex flex-col gap-4 min-w-md max-w-2xl"}>
                        <p className={"wrap-normal text-muted my-2"}>{translate("event_add_info")}</p>
                        <BandPicker
                            bandId={bandId}
                            onChange={band => {
                                setBandId(band.id)
                                setBandFormedYear(band.formedYear)
                            }}
                            strings={strings}
                        />


                        <CalendarDateField
                            isDisabled={bandId==null}
                            minValue={bandId!=null?minDate():null}
                            maxValue={maxDate}
                            value={date}
                            onChange={setDate}
                            label={translate("date")}
                            fieldError={translate("invalid_date")}
                            description={translate("choose_band_first")}
                        />

                        <TextField isRequired name={"name"} type={"text"} value={name} onChange={setName}>
                            <Label>{translate("event_name")}</Label>
                            <Input/>
                        </TextField>

                        <div className={"flex flex-col gap-1"}>
                            <Label>{translate("description")}</Label>
                            <TextArea
                                className={"min-w-md min-h-32"}
                                value={description}
                                placeholder={translate("enter_event_description")}
                                onChange={e => setDescription(e.target.value)}
                                aria-label="event description"
                            />
                        </div>
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