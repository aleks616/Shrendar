import React, { useState, useEffect } from 'react'
import { Surface, TextArea, Button } from '@heroui/react'
import { PencilIcon, CheckIcon } from '@heroicons/react/24/outline'

interface EditableSurfaceProps {
    value: string
    onSave?: (value: string) => void
}

export function EditableSurface({ value, onSave }: EditableSurfaceProps) {
    const [isEditing, setIsEditing] = useState(false)
    const [text, setText] = useState(value)

    useEffect(() => {
        setText(value)
    }, [value])

    const handleSave = () => {
        setIsEditing(false)
        onSave?.(text)
    }

    if (isEditing) {
        return (
            <div className="relative h-52 w-full">
                <TextArea
                    autoFocus
                    fullWidth
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    className="h-full w-full resize-none p-2 rounded-xl border-accent focus:outline-none"
                />
                <Button
                    isIconOnly
                    size="sm"
                    variant="ghost"
                    aria-label="Save"
                    onClick={handleSave}
                    className="absolute bottom-2 right-2 text-accent hover:text-accent/80"
                >
                    <CheckIcon className="h-5 w-5" />
                </Button>
            </div>
        )
    }

    return (
        <Surface
            className="h-52 w-full border-accent rounded-xl p-2 overflow-y-auto relative"
            variant="secondary"
        >
            <div className="whitespace-pre-wrap pr-8">
                {text}
            </div>
            <Button
                isIconOnly
                size="sm"
                variant="ghost"
                aria-label="Edit"
                onClick={() => setIsEditing(true)}
                className="absolute bottom-2 right-2 text-accent hover:text-accent/80"
            >
                <PencilIcon className="h-5 w-5" />
            </Button>
        </Surface>
    )
}