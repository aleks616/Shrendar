import React,{useState} from "react"
import {Navigate} from "react-router-dom"
import {Button,Form,Heading,Input,Label,Link,NumberField,TextArea,TextField} from "@heroui/react"
import {BandStatusPicker} from "../BandStatusPicker/BandStatusPicker.tsx"
import {CountryPicker} from "../CountryPicker/CountryPicker.tsx"
import {ImageField} from "../ImageField/ImageField.tsx"
import {BandAddDto,BandClient} from "sharedLogic"

export function AddBandForm({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [name,setName]=useState("")
    const [formedYear,setFormedYear]=useState<number | undefined>(undefined)
    const [status,setStatus]=useState<string>("unknown")
    const [disbandedYear,setDisbandedYear]=useState<number | undefined>(undefined)
    const [countryId,setCountryId]=useState<number | null>(null)
    const [description,setDescription]=useState("")
    const [imageUrl,setImageUrl]=useState("")
    const [resultText,setResultText]=useState("")

    const token=localStorage.getItem("token")
    if(token==null){
        return <Navigate to={"/404"}/>
    }

    const submit=async () => {
        if(name!=null&&formedYear!=undefined&&status!=null&&countryId!=null&&((status=="disbanded"&&disbandedYear!=undefined)||status!="disbanded")&&token!=null){
            const band=new BandAddDto(
                null,
                name,
                formedYear,
                status.toUpperCase(),
                disbandedYear,
                countryId,
                description,
                imageUrl
            )
            const result=await BandClient.getInstance().addBand(band,token)
            setResultText(result)
        }
    }
    const currentYear=new Date().getFullYear()

    function getBandId(resultText:string){
        return parseInt(resultText)
    }
    function isSuccess(resultText: string){
        return !isNaN(getBandId(resultText))
    }

    return (
        <div className={"w-9/12 flex flex-col gap-4"}>
            <Heading level={1} className={"text-center"}>{translate("enter_band_data")}</Heading>
            <Form>
                <main className={"w-full flex flex-col lg:flex-row justify-around"}>
                    <section className={"flex flex-col gap-4 min-w-md"}>
                        <TextField
                            isRequired
                            name={"band name"}
                            type={"text"}
                            value={name}
                            onChange={setName}>
                            <Label>{translate("band_name")}</Label>
                            <Input placeholder={translate("band_name")}/>
                        </TextField>

                        <NumberField
                            formatOptions={{useGrouping: false}}
                            isRequired
                            minValue={1901}
                            maxValue={currentYear}
                            value={formedYear}
                            onChange={setFormedYear}
                        >
                            <Label>{translate("formed_year")}</Label>
                            <NumberField.Group>
                                <NumberField.DecrementButton/>
                                <NumberField.Input placeholder={"1983"}/>
                                <NumberField.IncrementButton/>
                            </NumberField.Group>
                        </NumberField>

                        <BandStatusPicker
                            status={status}
                            onChange={setStatus}
                            onDisbandedYearChange={setDisbandedYear}
                            strings={strings}
                        />

                        {status=="disbanded"&&formedYear!=null&&(
                            <NumberField
                                formatOptions={{useGrouping: false}}
                                isRequired
                                minValue={formedYear}
                                maxValue={currentYear}
                                value={disbandedYear}
                                onChange={setDisbandedYear}
                            >
                                <Label>{translate("disbanded_year")}</Label>
                                <NumberField.Group>
                                    <NumberField.DecrementButton/>
                                    <NumberField.Input/>
                                    <NumberField.IncrementButton/>
                                </NumberField.Group>
                            </NumberField>
                        )}

                        <CountryPicker countryId={countryId} onChange={setCountryId} strings={strings}/>

                        <div className={"flex flex-col gap-1"}>
                            <Label>{translate("description")}</Label>
                            <TextArea
                                className={"min-w-md min-h-32"}
                                value={description}
                                placeholder={translate("band_description")}
                                onChange={e => setDescription(e.target.value)}
                                aria-label={translate("band_description")}
                            />
                        </div>
                    </section>
                    <section className={"flex flex-col gap-4 min-w-md"}>
                        <ImageField
                            imageUrl={imageUrl}
                            onChange={setImageUrl}
                            placeholder={translate("enter_photo_url")}
                            description={translate("band_photo_url")}
                            strings={strings}
                        />
                    </section>
                </main>
                <div className={"my-4 lg:flex items-center justify-center"}>
                    <Button className={"min-w-48"} onPress={submit}>{translate("add")}</Button>
                </div>
                {isSuccess(resultText)?(
                        <div className={"my-4 lg:flex items-center justify-center"}>
                            <Button className={"min-w-48"}>
                                <Link href={`band-add/${getBandId(resultText)}/members`} className={"text-white"}>
                                    {translate("add_band_members")}
                                </Link>
                            </Button>
                        </div>
                    ):
                    (
                    <p>{translate(resultText)}</p>

                    )}
            </Form>

        </div>
    )

}