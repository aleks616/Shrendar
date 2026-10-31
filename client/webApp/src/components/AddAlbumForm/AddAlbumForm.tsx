import {Button,Calendar,Collection,ComboBox,DateField,DatePicker,DateValue,
    Description,FieldError,Form,Heading,I18nProvider,InfoIcon,Input,Label,
    Link,ListBox,Select,TextArea,TextField,Tooltip} from "@heroui/react"
import {useAsyncList} from "@react-stately/data"
import React,{useEffect,useState} from "react"
import {AlbumAddDto,AlbumClient,BandAlbumAddDto,BandClient,Genre,GenreClient} from "sharedLogic"
import {CalendarDate} from "@internationalized/date"
import {Navigate} from "react-router-dom"

export function AddAlbumForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [genreList,setGenreList]=useState<Genre[] | null>(null)
    let [title,setTitle]=useState("")
    const [releaseDate,setReleaseDate]=useState<DateValue | null>(null)
    const [albumType,setAlbumType]=useState<string | null>(null)
    const [description,setDescription]=useState("")
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
    const [genre,setGenre]=useState<string | null>(null)
    const [importance,setImportance]=useState<number>(0)
    const [imageUrl,setImageUrl]=useState("")
    const [isImageValid,setIsImageValid]=useState(true)
    const [resultText,setResultText]=useState<string>("")

    useEffect(() => {
        const fetchGenreData=async () => {
            const genres=await GenreClient.getInstance().getAll()
            if(genres!=null){
                setGenreList(genres)
            }
        }
        fetchGenreData()
    },[])

    const token=localStorage.getItem("token")
    if(token==null){
       return <Navigate to={"/404"}/>
    }

    const albumTypes=["studio","ep","compilation","concert","demo","single","other"]

    function handleBandChange(e: any){
        const value=parseInt(e.toString())
        setBandId(value)
        const band=bandList.items.find(b => b.id==value)
        if(band!=null&&band.name!=null){
            bandList.setFilterText(band.name)
        }
    }

    function handleAlbumTypeChange(e: any){
        const value=e.toString()
        setAlbumType(value)
        if(value!="studio"&&value!="ep"){
            setImportance(0)
        }
        if(value=="ep"&&importance>3){
            setImportance(3)
        }
    }

    const minReleaseDate=() => {
        const bandFormedYear=bandList.items.find(b => b.id==bandId)?.formedYear
        return new CalendarDate(bandFormedYear!,1,1)
    }
    const today=new Date()
    const maxReleaseDate=new CalendarDate(today.getFullYear()+1,today.getMonth()+1,today.getDate())

    function invalidateImage(){
        setIsImageValid(false)
    }

    function handleUrlChange(e: any){
        setImageUrl(e)
        setIsImageValid(true)
    }

    const importanceRange=() => {
        if(albumType=="studio"){
            return [1,2,3,4,5]
        }
        else if(albumType=="ep"){
            return [1,2,3]
        }
        else{
            return [0]
        }
    }

    const submit=async() => {
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
                    <section className={"flex flex-col gap-4"}>
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
                                <ListBox renderEmptyState={() => bandList.filterText.length>3&&<p>{translate("no_results")}</p>}>
                                    <Collection items={bandList.items}>
                                        {(band) => {
                                            const label=band.country!=null ? band.name+" ("+translate(band.country)+")" : band.name
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
                        <TextField isRequired name={"title"} type={"text"} value={title} onChange={setTitle}>
                            <Label>{translate("album_title")}</Label>
                            <Input/>
                        </TextField>
                        <I18nProvider locale="en-GB">
                            <DatePicker
                                isDisabled={bandId==null}
                                isRequired
                                minValue={bandId!=null?minReleaseDate():null}
                                maxValue={maxReleaseDate}
                                className="w-72"
                                name="date"
                                value={releaseDate}
                                onChange={setReleaseDate}
                            >
                                <Label>{translate("release_date")}</Label>
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
                                    <Calendar aria-label="Event date">
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
                                {bandId==null&&<FieldError>{translate("choose_band_first")}</FieldError>}
                            </DatePicker>
                        </I18nProvider>
                        <Select isRequired name={"albumType"} value={albumType} placeholder={translate("choose_album_type")}
                                onChange={(e) => handleAlbumTypeChange(e)}>
                            <Label>{translate("album_type")}</Label>
                            <Select.Trigger>
                                <Select.Value>{albumType}</Select.Value>
                                <Select.Indicator/>
                            </Select.Trigger>
                            <Select.Popover>
                                <ListBox>
                                    {albumTypes.map((type) => (
                                        <ListBox.Item id={type} textValue={type}>
                                            {translate("album_type_"+type)}
                                            <ListBox.ItemIndicator/>
                                        </ListBox.Item>
                                    ))}
                                </ListBox>
                            </Select.Popover>
                        </Select>
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
                        <ComboBox
                            isRequired
                            allowsEmptyCollection
                            value={genre}
                            onChange={e => setGenre(e as string)}
                        >
                            <Label>
                                <div className={"flex flex-row gap-1 items-center"}>
                                    {translate("main_subgenre")}
                                    <Tooltip delay={0}>
                                        <Button variant={"ghost"} className={"p-0"}>
                                            <InfoIcon className={"size-4"}/>
                                        </Button>
                                        <Tooltip.Content>
                                            <p>{translate("select_main_subgenre")}</p>
                                        </Tooltip.Content>
                                    </Tooltip>
                                </div>
                            </Label>
                            <ComboBox.InputGroup>
                                <Input placeholder={translate("search_genre_dots")}/>
                                <ComboBox.Trigger/>
                            </ComboBox.InputGroup>
                            <ComboBox.Popover>
                                <ListBox renderEmptyState={() => <p>{translate("no_results")}</p>}>
                                    {genreList?.map((genre) => (
                                        <ListBox.Item id={genre.id} textValue={genre.name}>
                                            {genre.name}
                                            <ListBox.ItemIndicator/>
                                        </ListBox.Item>
                                    ))}
                                </ListBox>
                            </ComboBox.Popover>
                        </ComboBox>
                        <Select
                            isDisabled={albumType!="studio"&&albumType!="ep"}
                            name={"importance"}
                            value={importance.toString()}
                            onChange={(e) => setImportance(parseInt(e as string))}
                        >
                            <Label>
                                <div className={"flex flex-row gap-1 items-center max-w-md"}>
                                    {translate("album_importance")}
                                    <Tooltip delay={0}>
                                        <Button variant={"ghost"} className={"p-0"}>
                                            <InfoIcon className={"size-4"}/>
                                        </Button>
                                        <Tooltip.Content>
                                            <p className={"wrap-normal"}>{translate("album_importance_info")}</p>
                                        </Tooltip.Content>
                                    </Tooltip>
                                </div>
                            </Label>
                            <Select.Trigger>
                                <Select.Value>{importance}</Select.Value>
                                <Select.Indicator/>
                            </Select.Trigger>
                            <Select.Popover>
                                <ListBox>
                                    {importanceRange().map((value) => (
                                        <ListBox.Item key={value} id={value} textValue={value.toString()}>
                                            {value}
                                            <ListBox.ItemIndicator/>
                                        </ListBox.Item>
                                    ))}
                                </ListBox>
                            </Select.Popover>
                        </Select>
                        <TextField
                            name={"imageUrl"}
                            type={"text"}
                            value={imageUrl}
                            onChange={(e)=>handleUrlChange(e)}
                            isInvalid={imageUrl.length>255||!isImageValid}
                        >
                            <Label>
                                <div className={"flex flex-row gap-1 items-center"}>
                                    {translate("image_url")}
                                    <Tooltip delay={0}>
                                        <Button variant={"ghost"} className={"p-0"}>
                                            <InfoIcon className={"size-4"}/>
                                        </Button>
                                        <Tooltip.Content>
                                            <p>{translate("album_artwork_url_info")}</p>
                                        </Tooltip.Content>
                                    </Tooltip>
                                </div>
                            </Label>
                            <Input placeholder={"Enter image url"}/>
                            <FieldError>{translate("url_too_long")}</FieldError>
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