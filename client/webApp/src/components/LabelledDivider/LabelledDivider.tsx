import {Separator} from "@heroui/react";

export default function LabelledDivider({text}:{text:string}){
    return (
        <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-4">
            <Separator />
            <span className="text-sm text-muted">{text}</span>
            <Separator />
        </div>
    )
}