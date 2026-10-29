export const randomImageSrc=() => {
    const id=Math.floor(Math.random()*500)
    return "https://picsum.photos/200?random="+id
}