export const randomImageSrc=(seed?:string) => {
    if(seed){
        const readySeed=seed.replace(" ","_")
        return "https://picsum.photos/seed/"+readySeed+"/200"
    }
    else{
        const id=Math.floor(Math.random()*50)
        return "https://picsum.photos/200?random="+id
    }
}