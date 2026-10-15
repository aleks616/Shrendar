import {Card,Link} from "@heroui/react"
import {ArtistAnniversaryDto} from "sharedLogic"

export function ArtistBirthdayCard({strings,artist}:{strings:Record<string,string>,artist:ArtistAnniversaryDto}){
    const translate=(key: string) => strings[key]??key
    return(
        <div className={"mx-4 my-2"}>
            <Card>
                <Card.Header>
                    <Card.Title className={"text-xl font-semibold"}>{artist.yearsSince} 🎂</Card.Title>
                    <Card.Description className={"text-lg text-foreground"}>
                        <Link href={"/artist/"+artist.id}>{artist.name}</Link>
                    </Card.Description>
                </Card.Header>
                <Card.Content>
                    <p>{translate("birthday")}: {artist.anniversaryDate.toString()}</p>
                    {artist.country&&<p>{translate("country")}: {translate(artist.country)}</p>}
                    <div className={"flex flex-col justify-center items-center"}>
                        <div className={"w-72 h-72"}>
                            {artist.imageUrl
                                ?<img src={artist.imageUrl} alt={"artist photo"} className={"w-full h-full"}/>
                                :<img src={"no-image.jpg"} alt={"no image"} className={"w-full h-full"}/>
                            }
                        </div>
                    </div>
                </Card.Content>
                <Card.Footer />
            </Card>
        </div>
    )

}