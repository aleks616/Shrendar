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
            <Heading level={1} className={"text-center"}>Enter album data</Heading>
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
                            <Label>Band</Label>
                            <ComboBox.InputGroup>
                                <Input placeholder={"Search band..."}/>
                                <ComboBox.Trigger/>
                            </ComboBox.InputGroup>
                            <ComboBox.Popover>
                                <ListBox renderEmptyState={() => bandList.filterText.length>3&&<p>No results</p>}>
                                    <Collection items={bandList.items}>
                                        {(band) => {
                                            const label=band.name+" ("+band.country+")"
                                            return (
                                                <ListBox.Item id={band.id.toString()} textValue={label}>
                                                    <Link href={"../band/"+band.id} target={"_blank"}>{label}</Link>
                                                    <ListBox.ItemIndicator/>
                                                </ListBox.Item>
                                            )
                                        }}
                                    </Collection>
                                </ListBox>
                            </ComboBox.Popover>
                            <Description>Enter at least 2 characters</Description>
                        </ComboBox>
                        <TextField isRequired name={"title"} type={"text"} value={title} onChange={setTitle}>
                            <Label>Album title</Label>
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
                                <Label>Release date</Label>
                                <DateField.Group fullWidth>
                                    <DateField.Input>{(segment) => <DateField.Segment
                                        segment={segment}/>}</DateField.Input>
                                    <DateField.Suffix>
                                        <DatePicker.Trigger>
                                            <DatePicker.TriggerIndicator/>
                                        </DatePicker.Trigger>
                                    </DateField.Suffix>
                                </DateField.Group>
                                <FieldError>Release date can't be before band formed year or further in the future than 1 year</FieldError>
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
                                {bandId==null&&<FieldError>Choose band first</FieldError>}
                            </DatePicker>
                        </I18nProvider>
                        <Select isRequired name={"albumType"} value={albumType}
                                onChange={(e) => handleAlbumTypeChange(e)}>
                            <Label>Album type</Label>
                            <Select.Trigger>
                                <Select.Value>{albumType}</Select.Value>
                                <Select.Indicator/>
                            </Select.Trigger>
                            <Select.Popover>
                                <ListBox>
                                    {albumTypes.map((type) => (
                                        <ListBox.Item id={type} textValue={type}>
                                            {type}
                                            <ListBox.ItemIndicator/>
                                        </ListBox.Item>
                                    ))}
                                </ListBox>
                            </Select.Popover>
                        </Select>
                        <div className={"flex flex-col gap-1"}>
                            <Label>Description</Label>
                            <TextArea
                                className={"min-w-md min-h-32"}
                                value={description}
                                placeholder={"Enter album description"}
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
                                    Main subgenre
                                    <Tooltip delay={0}>
                                        <Button variant={"ghost"} className={"p-0"}>
                                            <InfoIcon className={"size-4"}/>
                                        </Button>
                                        <Tooltip.Content>
                                            <p>Select the main subgenre, there can be only one</p>
                                        </Tooltip.Content>
                                    </Tooltip>
                                </div>
                            </Label>
                            <ComboBox.InputGroup>
                                <Input placeholder={"Search genre..."}/>
                                <ComboBox.Trigger/>
                            </ComboBox.InputGroup>
                            <ComboBox.Popover>
                                <ListBox renderEmptyState={() => <p>No results</p>}>
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
                                    Importance
                                    <Tooltip delay={0}>
                                        <Button variant={"ghost"} className={"p-0"}>
                                            <InfoIcon className={"size-4"}/>
                                        </Button>
                                        <Tooltip.Content>
                                            <p className={"wrap-normal"}>The higher the number the more important,
                                                popular, influential it is</p>
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
                                    Image url
                                    <Tooltip delay={0}>
                                        <Button variant={"ghost"} className={"p-0"}>
                                            <InfoIcon className={"size-4"}/>
                                        </Button>
                                        <Tooltip.Content>
                                            <p>Paste url of the album artwork. You must be at least level 4 to do this.</p>
                                        </Tooltip.Content>
                                    </Tooltip>
                                </div>
                            </Label>
                            <Input placeholder={"Enter image url"}/>
                            <FieldError>Url can't be longer than 255 characters and must be valid.</FieldError>
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
                    <Button className={"min-w-48"} onPress={submit}>Add</Button>
                </div>
                <p>{translate(resultText)}</p>
            </Form>
        </div>
    )
}