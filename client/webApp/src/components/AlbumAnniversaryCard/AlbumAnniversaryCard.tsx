import {Card,Link} from "@heroui/react"
import {AlbumByDateDto} from "sharedLogic"
import {ImageWithFallback} from "../ImageWithFallback/ImageWithFallback.tsx"

export function AlbumAnniversaryCard({strings,album}:{strings:Record<string,string>,album:AlbumByDateDto}){
    const translate=(key: string) => strings[key]??key
    return(
        <div className={"mx-4 my-2"}>
            <Card>
                <Card.Header>
                    <Card.Title className={"text-xl font-semibold"}>{album.yearsSince} 🎂</Card.Title>
                    <Card.Description className={"text-lg text-foreground"}>
                        <Link href={"/album/"+album.id}>{album.band?.name} - {album.title}</Link>
                    </Card.Description>
                </Card.Header>
                <Card.Content>
                    <p>{translate("release_date")}: {album.releaseDate.toString()}</p>
                    <p>{translate("album_type")}: {album.type}</p>
                    {album.genre&&<p>{translate("main_genre")}: {album.genre?.name}</p>}
                    <div className={"flex flex-col justify-center items-center"}>
                        <div className={"w-72 h-72"}>
                            <ImageWithFallback src={album.artworkUrl} alt={album.title!} className={"w-full h-full"}/>
                        </div>
                    </div>
                </Card.Content>
                <Card.Footer />
            </Card>
        </div>
    )

}