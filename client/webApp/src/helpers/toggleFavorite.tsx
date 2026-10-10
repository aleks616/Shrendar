import {ProfileClient} from "sharedLogic"

export const toggleBandFavorite=async (id: number) => {
    const token=localStorage.getItem("token")
    const result=await ProfileClient.getInstance().toggleFavoriteBand(id,token)
    if(result!="band_toggled") console.error(result)
}

export const toggleArtistFavorite=async (id: bigint) => {
    const token=localStorage.getItem("token")
    const result=await ProfileClient.getInstance().toggleFavoriteArtist(id,token)
    if(result!="artist_toggled") console.error(result)
}

export const toggleGenreFavorite=async (id: number) => {
    const token=localStorage.getItem("token")
    const result=await ProfileClient.getInstance().toggleFavoriteGenre(id,token)
    if(result!="genre_toggled") console.error(result)
}

export const toggleArtistFavoriteAll=async (id: number) => {
    const token=localStorage.getItem("token")
    const result=await ProfileClient.getInstance().toggleFavoriteArtistAll(id,token)
    if(result!="artist_toggled") console.error(result)
}