import {Description,Label,ListBox,Select} from "@heroui/react"

export function AlbumImportancePicker({albumType,importance,onChange,strings}: {
    albumType: string | null
    importance: number
    onChange: (importance: number) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key

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

    return (
        <Select
            isDisabled={albumType!="studio"&&albumType!="ep"}
            name={"importance"}
            value={importance.toString()}
            onChange={(e) => onChange(parseInt(e as string))}
        >
            <Label>
                <div className={"flex flex-row gap-1 items-center max-w-md"}>
                    {translate("album_importance")}
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
            <Description>{translate("album_importance_info")}</Description>
        </Select>
    )
}
