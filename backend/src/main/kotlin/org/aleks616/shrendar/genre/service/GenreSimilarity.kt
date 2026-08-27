package org.aleks616.shrendar.genre.service

import org.springframework.stereotype.Component
import kotlin.math.abs
import kotlin.math.round

@Component
object GenreSimilarity{

    fun getGenreSimilarity(genre1:String,genre2:String):Double{
        var value=0.0
        for(i in 0..6){
            if(i<3) value+=abs(genre1[i]-genre2[i])
            else value+=0.667*abs(genre1[i]-genre2[i])
        }
        return "%.2f".format(value).toDouble()
    }

    fun getAverageGenre(genres:List<Pair<String,Byte>>):String{
        var result=""
        for(i in 0..6){
            var sum=0.0
            genres.forEach {
                sum+=it.first[i].digitToInt()
            }
            val avg=round((sum/genres.size)).toInt()
            result+=avg
        }
        return result
    }

}