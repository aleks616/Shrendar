import {ComboBox,Description,Input,Label,ListBox} from "@heroui/react"
import React,{useEffect,useState} from "react"
import {Genre,GenreClient} from "sharedLogic"

export function GenrePicker({genre,onChange,strings}: {
    genre: string | null
    onChange: (genre: string) => void
    strings: Record<string,string>
}){
    const translate=(key: string) => strings[key]??key
    const [genreList,setGenreList]=useState<Genre[] | null>(null)

    useEffect(() => {
        const fetchGenreData=async () => {
            const genres=await GenreClient.getInstance().getAll()
            if(genres!=null){
                setGenreList(genres)
            }
        }
        fetchGenreData()
    },[])

    return (
        <ComboBox
            isRequired
            allowsEmptyCollection
            value={genre}
            onChange={e => onChange(e as string)}
        >
            <Label>
                {translate("main_subgenre")}
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
            <Description>{translate("select_main_subgenre")}</Description>
        </ComboBox>
    )
}
