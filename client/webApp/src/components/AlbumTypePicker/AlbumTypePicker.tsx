import {Label,ListBox,Select} from "@heroui/react"

export function AlbumTypePicker({albumType,importance,onAlbumTypeChange,onImportanceChange,strings}: {
    albumType: string | null
    importance: number
    onAlbumTypeChange: (albumType: string) => void
    onImportanceChange: (importance: number) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const albumTypes=["studio","ep","compilation","concert","demo","single","other"]

    function handleAlbumTypeChange(e: any){
        const value=e.toString()
        onAlbumTypeChange(value)
        if(value!="studio"&&value!="ep"){
            onImportanceChange(0)
        }
        if(value=="ep"&&importance>3){
            onImportanceChange(3)
        }
    }

    return (
        <Select isRequired name={"albumType"} value={albumType}
                placeholder={translate("choose_album_type")}
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
    )
}
