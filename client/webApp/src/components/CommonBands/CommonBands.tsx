import {CommonBandsDto,HomepageClient} from "sharedLogic"
import React,{useEffect,useState} from "react"
import {Heading,Link,Skeleton} from "@heroui/react"

export function CommonBands({strings}: { strings: Record<string,string> }){
    const translate=(key: string) => strings[key]??key
    const [data,setData]=useState<CommonBandsDto | null>(null)
    const [isLoading,setIsLoading]=useState(true)

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            const token=localStorage.getItem("token")
            if(token==null){
                setIsLoading(false)
                return
            }
            const data=await HomepageClient.getInstance().getCommonBands(token)
            if(data!=null) setData(data)
            setIsLoading(false)
        }
        fetchData()
    },[])

    if(isLoading){
        return (
            <div className={"mx-3 my-2 flex flex-col gap-2 skeleton--shimmer"}>
                <section className={"flex flex-col gap-4"}>
                    <Skeleton animationType={"none"} className={"h-8 w-56 rounded-md"}/>
                    {Array.from({length:8}).map((_,index) => (
                        <div key={index} className={"flex flex-col gap-2"}>
                            <Skeleton animationType={"none"} className={"h-4 w-4/7 rounded-md"}/>
                            <Skeleton animationType={"none"} className={"h-4 w-5/7 rounded-md"}/>
                            <Skeleton animationType={"none"} className={"h-4 w-3/7 rounded-md"}/>
                        </div>
                    ))}
                </section>
            </div>
        )
    }

    if(data==null){
        return <></>
    }

    const bands=data?.bands?.asJsReadonlyArrayView()??[]

    return (
        <div className={"mx-3 my-2 flex flex-col gap-2"}>
            {bands.length>0&&
                <section className={"flex flex-col gap-4"}>
                    <Heading level={3}>{translate("bands_to_check_out")}</Heading>
                    {bands.map((band,index) => (
                        <div key={index}>
                            <Link href={"/band/"+band.bandId}>{band.bandName}</Link>
                            <p>{translate("member_in_favorites")}: </p>
                            <Link href={"/artist/"+band.artistId}>{band.nickname??band.artistName}</Link>
                        </div>
                    ))}
                </section>}
        </div>
    )
}