import {randomImageSrc} from "../../helpers/randomImage.ts"
import {Nullable} from "sharedLogic"

export function ImageWithFallback({src,alt,className}: { src: Nullable<string>,alt: string,className: string }){
    return (
        <div className={className}>
            {src?
                <img src={src} alt={alt} className={className}/>
                :<div className={"relative inline-block overflow-hidden "+className}>
                    <p className={"absolute left-1/2 top-1/2 z-10 -translate-x-1/2 -translate-y-1/2 -rotate-45 whitespace-nowrap text-red-600 text-6xl font-bold"}>
                        SAMPLE
                    </p>
                    <img src={randomImageSrc()} alt={"no image"} className={"block max-w-full h-auto opacity-80 filter: grayscale-75 "+className}/>
                </div>
            }
        </div>
    )
}