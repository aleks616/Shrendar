import {
    Button,
    Calendar,
    Collection,
    ComboBox,
    DateField,
    DatePicker,DateValue,
    Description,
    FieldError,Form,
    Heading,I18nProvider,
    Input,
    Label,
    Link,
    ListBox,TextArea,TextField
} from "@heroui/react"
import React,{useState} from "react"
import {useAsyncList} from "@react-stately/data"
import {BandAlbumAddDto,BandClient,EventAddDto,EventClient} from "sharedLogic"
import {CalendarDate} from "@internationalized/date"
import {Navigate} from "react-router-dom";

export function AddEventForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [bandId,setBandId]=useState<number | null>(null)
    const bandList=useAsyncList<BandAlbumAddDto>({
        async load({filterText}){
            if(filterText!=null){
                if(filterText.length<2){
                    return {items: []}
                }
                const bands=await BandClient.getInstance().getBandDataForAlbumAdding(filterText)
                return {items: bands.asJsReadonlyArrayView()}
            }
            return {items: []}
        }
    })
    const [date,setDate]=useState<DateValue | null>(null)
    const [name,setName]=useState<string>("")
    const [description,setDescription]=useState<string>("")
    const [resultText,setResultText]=useState<string>("")

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }

    function handleBandChange(e: any){
        const value=parseInt(e.toString())
        setBandId(value)
        const band=bandList.items.find(b => b.id==value)
        if(band!=null&&band.name!=null){
            bandList.setFilterText(band.name)
        }
    }

    const minDate=() => {
        const bandFormedYear=bandList.items.find(b => b.id==bandId)?.formedYear
        return new CalendarDate(bandFormedYear!,1,1)
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

    return(
        <div className={"w-9/12 flex flex-col gap-4"}>
            <Form>
                <Heading level={1} className={"text-center"}>{translate("enter_event_data")}</Heading>
                <div className={"w-full flex justify-center"}>
                    <section className={"flex flex-col gap-4 min-w-md max-w-2xl"}>
                        <p className={"wrap-normal text-muted my-2"}>{translate("event_add_info")}</p>
                        <ComboBox
                            menuTrigger="input"
                            allowsEmptyCollection
                            isRequired
                            value={bandId?.toString()??null}
                            inputValue={bandList.filterText??""}
                            onInputChange={bandList.setFilterText}
                            onChange={e => handleBandChange(e)}
                        >
                            <Label>{translate("band")}</Label>
                            <ComboBox.InputGroup>
                                <Input placeholder={translate("search_band_dots")}/>
                                <ComboBox.Trigger/>
                            </ComboBox.InputGroup>
                            <ComboBox.Popover>
                                <ListBox renderEmptyState={() => bandList.filterText.length>3&&
                                    <p>{translate("no_results")}</p>}>
                                    <Collection items={bandList.items}>
                                        {(band) => {
                                            const label=band.country!=null?band.name+" ("+translate(band.country)+")":band.name
                                            return (
                                                <ListBox.Item id={band.id.toString()} textValue={label??""}>
                                                    <Link href={"../band/"+band.id} target={"_blank"}>{label}</Link>
                                                    <ListBox.ItemIndicator/>
                                                </ListBox.Item>
                                            )
                                        }}
                                    </Collection>
                                </ListBox>
                            </ComboBox.Popover>
                            <Description>{translate("enter_2_plus_characters")}</Description>
                        </ComboBox>

                        <I18nProvider locale="en-GB">
                            <DatePicker
                                isDisabled={bandId==null}
                                isRequired
                                minValue={bandId!=null?minDate():null}
                                maxValue={maxDate}
                                className="w-72"
                                name="date"
                                value={date}
                                onChange={setDate}
                            >
                                <Label>{translate("date")}</Label>
                                <DateField.Group fullWidth>
                                    <DateField.Input>{(segment) => <DateField.Segment
                                        segment={segment}/>}</DateField.Input>
                                    <DateField.Suffix>
                                        <DatePicker.Trigger>
                                            <DatePicker.TriggerIndicator/>
                                        </DatePicker.Trigger>
                                    </DateField.Suffix>
                                </DateField.Group>
                                <FieldError>{translate("invalid_date")}</FieldError>
                                <DatePicker.Popover>
                                    <Calendar aria-label={translate("event_date")}>
                                        <Calendar.Header>
                                            <Calendar.YearPickerTrigger>
                                                <Calendar.YearPickerTriggerHeading/>
                                                <Calendar.YearPickerTriggerIndicator/>
                                            </Calendar.YearPickerTrigger>
                                            <Calendar.NavButton slot="previous"/>
                                            <Calendar.NavButton slot="next"/>
                                        </Calendar.Header>
                                        <Calendar.Grid>
                                            <Calendar.GridHeader>
                                                {(day) => <Calendar.HeaderCell>{day}</Calendar.HeaderCell>}
                                            </Calendar.GridHeader>
                                            <Calendar.GridBody>{(date) => <Calendar.Cell
                                                date={date}/>}</Calendar.GridBody>
                                        </Calendar.Grid>
                                        <Calendar.YearPickerGrid>
                                            <Calendar.YearPickerGridBody>
                                                {({year}) => <Calendar.YearPickerCell year={year}/>}
                                            </Calendar.YearPickerGridBody>
                                        </Calendar.YearPickerGrid>
                                    </Calendar>
                                </DatePicker.Popover>
                                {bandId==null&&<Description>{translate("choose_band_first")}</Description>}
                            </DatePicker>
                        </I18nProvider>

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