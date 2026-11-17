import {ComboBox,Description,Input,Label,ListBox} from "@heroui/react"
import React,{useEffect,useState} from "react"
import {ArtistClient,CountryDto} from "sharedLogic"

export function CountryPicker({countryId,onChange,strings}: {
    countryId: number | null
    onChange: (countryId: number) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const [countryList,setCountryList]=useState<CountryDto[] | null>(null)

    useEffect(() => {
        const fetchCountryList=async () => {
            const countries=await ArtistClient.getInstance().getCountries()
            if(countries!=null){
                setCountryList(countries)
            }
        }
        fetchCountryList()
    },[])

    return (
        <ComboBox
            isRequired
            allowsEmptyCollection
            value={countryId}
            onChange={e => onChange(parseInt(e as string))}
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
    )
}
