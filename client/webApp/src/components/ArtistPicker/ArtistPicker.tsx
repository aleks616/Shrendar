import {Collection,ComboBox,Description,Input,Label,Link,ListBox} from "@heroui/react"
import {useAsyncList} from "@react-stately/data"
import {ArtistClient,ArtistMemberAddDto} from "sharedLogic"

export function ArtistPicker({artistId,onChange,strings}: {
    artistId: bigint | null
    onChange: (artist: ArtistMemberAddDto) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const artistList=useAsyncList<ArtistMemberAddDto>({
        async load({filterText}){
            if(filterText!=null){
                if(filterText.length<3){
                    return {items: []}
                }
                const artists=await ArtistClient.getInstance().getArtistDataForMemberAdd(filterText)
                return {items: artists.asJsReadonlyArrayView()}
            }
            return {items: []}
        }
    })

    function handleArtistChange(e: any){
        const artist=artistList.items.find(a => a.id.toString()==e.toString())
        if(artist!=null){
            onChange(artist)
            if(artist.name!=null){
                artistList.setFilterText(artist.name)
            }
        }
    }

    return (
        <ComboBox
            menuTrigger="input"
            allowsEmptyCollection
            isRequired
            value={artistId?.toString()??null}
            inputValue={artistList.filterText??""}
            onInputChange={artistList.setFilterText}
            onChange={e => handleArtistChange(e)}
        >
            <Label>{translate("artist_name")}</Label>
            <ComboBox.InputGroup>
                <Input placeholder={translate("search_artist_dots")}/>
                <ComboBox.Trigger/>
            </ComboBox.InputGroup>
            <ComboBox.Popover>
                <ListBox renderEmptyState={() => artistList.filterText.length>3&&
                    <p>{translate("no_results")}</p>}>
                    <Collection items={artistList.items}>
                        {(artist) => {
                            const label=artist.country!=null?artist.name+" ("+translate(artist.country)+")":artist.name
                            return (
                                <ListBox.Item id={artist.id.toString()} textValue={label??""}>
                                    <Link href={"../artist/"+artist.id} target={"_blank"}>{label}</Link>
                                    <ListBox.ItemIndicator/>
                                </ListBox.Item>
                            )
                        }}
                    </Collection>
                </ListBox>
            </ComboBox.Popover>
            <Description>{translate("enter_3_plus_characters")}</Description>
        </ComboBox>
    )
}
