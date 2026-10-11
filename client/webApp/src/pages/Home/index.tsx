import {AppHeader} from "../../components/AppHeader/AppHeader.tsx";
import {Heading} from "@heroui/react";

export function Home() {
    return (
        <>
            <AppHeader />
            <div className={"flex justify-center w-svw"}>
                <Heading level={1}>Home page</Heading>
            </div>
        </>
    )
}