import {
    Button,
    Calendar,
    Checkbox,
    ComboBox,
    DateField,
    DatePicker,
    DateValue,
    Description,
    FieldError,
    Form,
    Heading,
    I18nProvider,
    Input,
    Label,
    ListBox,
    Select,
    TextArea,
    TextField
} from "@heroui/react"
import React,{useEffect,useState} from "react"
import {ArtistAddDto,ArtistClient,CountryDto} from "sharedLogic"
import {Navigate} from "react-router-dom"
import {CalendarDate} from "@internationalized/date"

export function AddArtistForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [name,setName]=useState("")
    const [birthdate,setBirthdate]=useState<DateValue | null>(null)
    const [isDead,setIsDead]=useState(false)
    const [deathdate,setDeathdate]=useState<DateValue | null>(null)
    const genderOptions=["M","F","X"]
    const [gender,setGender]=useState<string>("M")

    const [countryList,setCountryList]=useState<CountryDto[] | null>(null)
    const [countryId,setCountryId]=useState<number | null>(null)
    const [description,setDescription]=useState("")
    const [imageUrl,setImageUrl]=useState("")
    const [isImageValid,setIsImageValid]=useState(true)
    const [resultText,setResultText]=useState("")

    useEffect(() => {
        const fetchCountryList=async () => {
            const countries=await ArtistClient.getInstance().getCountries()
            if(countries!=null){
                setCountryList(countries)
            }
        }
        fetchCountryList()
    },[])

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }
    const today=new Date()
    const maxBirthDate=new CalendarDate(today.getFullYear()-10,today.getMonth(),today.getDate())
    const minDeathDate=birthdate!=null?new CalendarDate(birthdate.year+10,birthdate.month,birthdate.day):null
    const maxDeathDate=new CalendarDate(today.getFullYear(),today.getMonth(),today.getDate())

    function handleGenderChange(e: any){
        const value=e as string
        if(genderOptions.includes(value)){
            setGender(value)
        }
    }

    function invalidateImage(){
        setIsImageValid(false)
    }

    function handleUrlChange(e: any){
        setImageUrl(e)
        setIsImageValid(true)
    }

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

    const genderKey=(gender: string): string => {
        switch(gender){
            case "M":
                return translate("male")
            case "F":
                return translate("female")
            default:
                return translate("gender_x")
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

                        <I18nProvider locale={"en-GB"}>
                            <DatePicker
                                isRequired
                                maxValue={maxBirthDate}
                                className={"w-72"}
                                name={"birthdate"}
                                value={birthdate}
                                onChange={setBirthdate}
                            >
                                <Label>{translate("birthdate")}</Label>
                                <DateField.Group fullWidth>
                                    <DateField.Input>{(segment) => <DateField.Segment
                                        segment={segment}/>}</DateField.Input>
                                    <DateField.Suffix>
                                        <DatePicker.Trigger>
                                            <DatePicker.TriggerIndicator/>
                                        </DatePicker.Trigger>
                                    </DateField.Suffix>
                                </DateField.Group>
                                <FieldError>{translate("invalid_artist_birthdate")}</FieldError>
                                <DatePicker.Popover>
                                    <Calendar aria-label={translate("birthdate")}>
                                        <Calendar.Header>
                                            <Calendar.YearPickerTrigger>
                                                <Calendar.YearPickerTriggerHeading/>
                                                <Calendar.YearPickerTriggerIndicator/>
                                            </Calendar.YearPickerTrigger>
                                            <Calendar.NavButton slot={"previous"}/>
                                            <Calendar.NavButton slot={"next"}/>
                                        </Calendar.Header>
                                        <Calendar.Grid>
                                            <Calendar.GridHeader>
                                                {(day) => <Calendar.HeaderCell>{day}</Calendar.HeaderCell>}
                                            </Calendar.GridHeader>
                                            <Calendar.GridBody>
                                                {(date) => <Calendar.Cell date={date}/>}
                                            </Calendar.GridBody>
                                        </Calendar.Grid>
                                        <Calendar.YearPickerGrid>
                                            <Calendar.YearPickerGridBody>
                                                {({year}) => <Calendar.YearPickerCell year={year}/>}
                                            </Calendar.YearPickerGridBody>
                                        </Calendar.YearPickerGrid>
                                    </Calendar>
                                </DatePicker.Popover>
                            </DatePicker>
                        </I18nProvider>

                        <Checkbox id={"is-dead"} isSelected={isDead} onChange={setIsDead}>
                            <Checkbox.Content>
                                <Checkbox.Control>
                                    <Checkbox.Indicator/>
                                </Checkbox.Control>
                                {translate("is_dead")}
                            </Checkbox.Content>
                        </Checkbox>

                        {isDead&&(
                            <I18nProvider locale={"en-GB"}>
                                <DatePicker
                                    isInvalid={(deathdate==null&&isDead)||(deathdate!=null&&birthdate!=null&&deathdate<birthdate)||(deathdate!=null&&deathdate>maxDeathDate)}
                                    isRequired
                                    minValue={minDeathDate}
                                    maxValue={maxDeathDate}
                                    className={"w-72"}
                                    name={"deathdate"}
                                    value={deathdate}
                                    onChange={setDeathdate}
                                >
                                    <Label>{translate("death_date")}</Label>
                                    <DateField.Group fullWidth>
                                        <DateField.Input>{(segment) => <DateField.Segment
                                            segment={segment}/>}</DateField.Input>
                                        <DateField.Suffix>
                                            <DatePicker.Trigger>
                                                <DatePicker.TriggerIndicator/>
                                            </DatePicker.Trigger>
                                        </DateField.Suffix>
                                    </DateField.Group>
                                    <FieldError>{translate("invalid_death_date")}</FieldError>
                                    <DatePicker.Popover>
                                        <Calendar aria-label={translate("death_date")}>
                                            <Calendar.Header>
                                                <Calendar.YearPickerTrigger>
                                                    <Calendar.YearPickerTriggerHeading/>
                                                    <Calendar.YearPickerTriggerIndicator/>
                                                </Calendar.YearPickerTrigger>
                                                <Calendar.NavButton slot={"previous"}/>
                                                <Calendar.NavButton slot={"next"}/>
                                            </Calendar.Header>
                                            <Calendar.Grid>
                                                <Calendar.GridHeader>
                                                    {(day) => <Calendar.HeaderCell>{day}</Calendar.HeaderCell>}
                                                </Calendar.GridHeader>
                                                <Calendar.GridBody>
                                                    {(date) => <Calendar.Cell date={date}/>}
                                                </Calendar.GridBody>
                                            </Calendar.Grid>
                                            <Calendar.YearPickerGrid>
                                                <Calendar.YearPickerGridBody>
                                                    {({year}) => <Calendar.YearPickerCell year={year}/>}
                                                </Calendar.YearPickerGridBody>
                                            </Calendar.YearPickerGrid>
                                        </Calendar>
                                    </DatePicker.Popover>
                                </DatePicker>
                            </I18nProvider>
                        )}

                        <Select isRequired name={"gender"} value={gender}
                                placeholder={translate("select_gender")}
                                onChange={(e) => handleGenderChange(e)}
                        >
                            <Label>{translate("gender")}</Label>
                            <Select.Trigger>
                                <Select.Value>{gender}</Select.Value>
                                <Select.Indicator/>
                            </Select.Trigger>
                            <Select.Popover>
                                <ListBox>
                                    {genderOptions.map((option) => (
                                        <ListBox.Item id={option} textValue={option}>
                                            {genderKey(option)}
                                            <ListBox.ItemIndicator/>
                                        </ListBox.Item>
                                    ))}
                                </ListBox>
                            </Select.Popover>
                        </Select>
                    </section>
                    <section className={"flex flex-col gap-4 min-w-md"}>

                        <ComboBox
                            isRequired
                            allowsEmptyCollection
                            value={countryId}
                            onChange={e => setCountryId(parseInt(e as string))}
                        >
                            <Label>
                                {translate("country")}
                            </Label>
                            <ComboBox.InputGroup>
                                <Input placeholder={translate("search_country")}/>
                            </ComboBox.InputGroup>
                            <ComboBox.Popover>
                                <ListBox renderEmptyState={() => <p>{translate("no_results")}</p>}>
                                    {countryList?.map((country) => (
                                        <ListBox.Item id={country.id} textValue={translate(country.name)}>
                                            {translate(country.name)}
                                            <ListBox.ItemIndicator/>
                                        </ListBox.Item>
                                    ))}
                                </ListBox>
                            </ComboBox.Popover>
                            <Description>{translate("select_country")}</Description>
                        </ComboBox>

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

                        <TextField
                            name={"imageUrl"}
                            type={"text"}
                            value={imageUrl}
                            onChange={(e) => handleUrlChange(e)}
                            isInvalid={imageUrl.length>255|| !isImageValid}
                        >
                            <Label>
                                {translate("image_url")}
                            </Label>
                            <Input placeholder={translate("enter_photo_url")}/>
                            <FieldError>{translate("url_too_long")}</FieldError>
                            <Description>{translate("artist_photo_url")}</Description>
                        </TextField>

                        {imageUrl.length>0?
                            <div className={"max-w-72"}>
                                <img src={imageUrl} alt={"preview"} onError={invalidateImage}/>
                            </div>
                            :
                            <div className={"w-72 h-72 bg-background-secondary border-2"}/>
                        }

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