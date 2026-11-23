import {Label,ListBox,Select} from "@heroui/react"

export function BandStatusPicker({status,onChange,onDisbandedYearChange,strings}: {
    status: string
    onChange: (status: string) => void
    onDisbandedYearChange: (year: number | undefined) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const statuses=["active","disbanded","on_hold","unknown"]

    function handleStatusChange(e: any){
        const value=e.toString()
        onChange(value)
        if(value!="disbanded") onDisbandedYearChange(undefined)
    }

    return (
        <Select
            isRequired
            name={"status"}
            placeholder={"band status"}
            value={status}
            onChange={(e) => handleStatusChange(e)}
        >
            <Label>{translate("status")}</Label>
            <Select.Trigger>
                <Select.Value>{translate(status)}</Select.Value>
                <Select.Indicator/>
            </Select.Trigger>
            <Select.Popover>
                <ListBox>
                    {statuses.map((status) => (
                        <ListBox.Item id={status} textValue={status}>
                            {translate(status)}
                            <ListBox.ItemIndicator/>
                        </ListBox.Item>
                    ))}
                </ListBox>
            </Select.Popover>
        </Select>
    )
}
