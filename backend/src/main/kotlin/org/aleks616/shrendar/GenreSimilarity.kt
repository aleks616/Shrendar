package org.aleks616.shrendar

import org.springframework.stereotype.Component
import kotlin.math.abs


@Component
object GenreSimilarity{

    fun getGenreSimilarity(genre1:Int,genre2:Int):Double{
        val genre1String=genre1.toString()
        val genre2String=genre2.toString()
        return getGenreSimilarity(genre1String,genre2String)
    }

    fun getGenreSimilarity(genre1:String,genre2:String):Double{
        var value=0.0
        for(i in 0..6){
            if(i<3) value+=abs(genre1[i]-genre2[i])
            else value+=0.667*abs(genre1[i]-genre2[i])
        }
        return "%.2f".format(value).toDouble()
    }

}