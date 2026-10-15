import {Card,Link} from "@heroui/react"
import {EventWikiDto} from "sharedLogic"

export function EventAnniversaryCard({strings,event}:{strings:Record<string,string>,event:EventWikiDto}){
    const translate=(key: string) => strings[key]??key
    return(
        <div className={"mx-4 my-2 w-80"}>
            <Card>
                <Card.Header>
                    <Card.Title className={"text-xl font-semibold"}>{event.yearsSince} 🎤</Card.Title>
                    <Card.Description className={"text-lg text-foreground"}>
                        <Link href={"/event/"+event.id}>{event.name}</Link>
                    </Card.Description>
                </Card.Header>
                <Card.Content>
                    <p>{translate("date")}: {event.date.toString()}</p>
                    <Link href={"/band/"+event.bandId}>{translate("band")}: {event.bandName}</Link>
                </Card.Content>
                <Card.Footer />
            </Card>
        </div>
    )

}