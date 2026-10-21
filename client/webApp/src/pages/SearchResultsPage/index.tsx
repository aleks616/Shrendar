import {getLanguage} from "../../components/getLanguage.ts"
import englishStrings from 'sharedLogic/localization/comexampleclient_stringsJson.json'
import polishStrings from 'sharedLogic/localization/comexampleclient_stringsJson_pl.json'
import {AppHeader} from "../../components/AppHeader/AppHeader.tsx"
import React,{useEffect,useState} from "react"
import {SearchClient,SearchResultItem,SearchResults} from "sharedLogic"
import {Heading,Link,Spinner} from "@heroui/react"
import {useParams} from "react-router-dom"

export function SearchResultsPage(){
    const query=useParams().query!!
    const lang=getLanguage().toUpperCase()
    const strings: Record<string,string>=lang==="PL"?polishStrings:englishStrings
    const translate=(key: string) => strings[key]??key
    const [data,setData]=useState<SearchResults | null>(null)
    const [isLoading,setIsLoading]=useState(true)
    const filterOptions=["all","band","album","artist","user","event"]
    const [filter,setFilter]=useState("all")
    const [showingItems,setShowingItems]=useState<SearchResultItem[] | null>(null)

    useEffect(() => {
        const fetchData=async () => {
            setIsLoading(true)
            const data=await SearchClient.getInstance().search(query)
            if(data!=null) setData(data)
            const allItems=data?.items?.asJsReadonlyArrayView()??[]
            // @ts-ignore
            setShowingItems(allItems)
            setIsLoading(false)
        }
        fetchData()
    },[query])

    console.log(data)

    if(isLoading){
        return (
            <div className="flex justify-center items-center p-8">
                <Spinner size="lg" color="accent"/>
            </div>
        )
    }

    const allItems=data?.items?.asJsReadonlyArrayView()??[]

    return (
        <>
            <AppHeader/>
            {allItems.length>0?
                <div className={"w-svw flex justify-between"}>
                    <aside className={"p-4 w-1/7 flex flex-col gap-4 text-lg bg-surface"}>
                        <Heading level={4}>{translate("filters")}</Heading>
                        <div
                            role="radiogroup"
                            aria-label="select filter"
                            className="flex flex-col h-fit items-center gap-1 rounded-md p-1"
                        >
                            {filterOptions.map((option,index) => (
                                    <button
                                        key={index}
                                        role="radio"
                                        aria-checked={filter===option}
                                        onClick={() => {
                                            setFilter(option)
                                            if(option!="all"){
                                                setShowingItems(allItems.filter(item => item.type===option))
                                            }
                                            else{
                                                // @ts-ignore
                                                setShowingItems(allItems)
                                            }
                                        }}
                                        className={(filter===option?
                                            "ps-2 text-start w-full h-10 rounded-sm bg-accent-soft":
                                            "ps-2 text-start w-full h-10 bg-surface")}
                                    >
                                        {translate("filter_"+option)}
                                    </button>
                                )
                            )}
                        </div>
                    </aside>
                    <div className={"w-1/5"}/>
                    <main className={"w-3/7 flex flex-col gap-5 min-h-svh mt-4"}>
                        {showingItems?.map((item,index) => (
                            <div key={index} className={"flex bg-surface p-2 rounded-lg shadow max-w-3xl"}>
                                <div className={"flex items-center gap-3"}>
                                    <section className={"max-w-16 max-h-16"}>
                                        <Link href={item.link??""}>
                                            {item.imageUrl
                                                ?<img src={item.imageUrl} alt={item.name!!}
                                                      className={"aspect-square rounded-full"}/>
                                                :<img src={"/no-image.jpg"} alt={"no image"}
                                                      className={"aspect-square rounded-full"}/>
                                            }
                                        </Link>
                                    </section>
                                    <section>
                                        <Link href={item.link??""}>
                                            <p className={"font-semibold text-lg"}>{item.name}</p>
                                        </Link>
                                        {item.description!="null..."?
                                            <p className={"text-muted text-sm"}>{item.description}</p>
                                            :<p className={"invisible"}>none</p>}
                                    </section>
                                </div>
                            </div>
                        ))}
                        {showingItems?.length===0&& <div className={"w-full flex justify-center"}>
                            <Heading level={1} className={"mt-4"}>{translate("no_results")}</Heading>
                        </div>}
                    </main>
                    <aside className={"w-2/7"}></aside>
                </div>
                :
                <div className={"w-full flex justify-center"}>
                    <Heading level={1} className={"mt-4"}>{translate("no_results")}</Heading>
                </div>
            }
        </>
    )
}