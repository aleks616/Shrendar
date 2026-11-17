import {Label,ListBox,Select} from "@heroui/react"

export function GenderPicker({gender,onChange,strings}: {
    gender: string
    onChange: (gender: string) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const genderOptions=["M","F","X"]

    function handleGenderChange(e: any){
        const value=e as string
        if(genderOptions.includes(value)){
            onChange(value)
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
    )
}
