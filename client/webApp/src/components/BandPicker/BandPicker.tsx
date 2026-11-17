import {Collection,ComboBox,Description,Input,Label,Link,ListBox} from "@heroui/react"
import {useAsyncList} from "@react-stately/data"
import {BandAlbumAddDto,BandClient} from "sharedLogic"

export function BandPicker({bandId,onChange,strings}: {
    bandId: number | null
    onChange: (band: BandAlbumAddDto) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
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

    function handleBandChange(e: any){
        const value=parseInt(e.toString())
        const band=bandList.items.find(b => b.id==value)
        if(band!=null){
            onChange(band)
            if(band.name!=null){
                bandList.setFilterText(band.name)
            }
        }
    }

    return (
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
    )
}
