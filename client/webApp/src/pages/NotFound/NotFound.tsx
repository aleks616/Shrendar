import {AppHeader} from "../../components/AppHeader/AppHeader.tsx";
import {Heading} from "@heroui/react";

export function NotFound() {
    return (
        <>
            <AppHeader/>
            <main className={"flex justify-center w-svw h-svh"}>
                <Heading level={1}>Not found 404</Heading>
            </main>
        </>
    )
}
