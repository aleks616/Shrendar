import React,{useEffect,useState} from 'react'
import {Button,Surface,TextArea} from '@heroui/react'
import {CheckIcon,PencilIcon} from '@heroicons/react/24/outline'

interface EditableSurfaceProps {
    value: string
    onSave?: (value: string) => void
    canEdit?: boolean
}

export function EditableSurface({value,onSave,canEdit}: EditableSurfaceProps){
    const [isEditing,setIsEditing]=useState(false)
    const [text,setText]=useState(value)

    useEffect(() => {
        setText(value)
    },[value])

    const handleSave=() => {
        setIsEditing(false)
        onSave?.(text)
    }


    return (
        <div className="relative h-52 max-w-full lg:w-3xl">
            {isEditing?(
                    <>
                        <TextArea
                            autoFocus
                            fullWidth
                            value={text}
                            onChange={(e) => setText(e.target.value)}
                            className="h-full w-full resize-none p-2 rounded-xl border-accent focus:outline-none text-sm"
                        />
                        <Button
                            isIconOnly
                            size="sm"
                            variant="ghost"
                            aria-label="Save"
                            onClick={handleSave}
                            className="absolute bottom-2 right-2 text-accent hover:text-accent/80"
                        >
                            <CheckIcon className="h-5 w-5"/>
                        </Button>
                    </>

                ):
                (
                    <Surface
                        className="relative h-52 w-full border-accent rounded-xl overflow-y-auto text-sm p-2"
                        variant="secondary"
                    >
                        <div className="whitespace-pre-wrap">
                            {text}
                        </div>
                        {canEdit&&
                            <Button
                                isIconOnly
                                size="sm"
                                variant="ghost"
                                aria-label="Edit"
                                onClick={() => setIsEditing(true)}
                                className="sticky bottom-2 ml-auto flex z-10 text-accent hover:text-accent/80"
                            >
                                <PencilIcon className="h-5 w-5"/>
                            </Button>}
                    </Surface>
                )}

        </div>
    )
}