package org.aleks616.shrendar

import org.springframework.stereotype.Component
import kotlin.math.abs


@Component
object GenreSimilarity{

    fun getGenreSimilarity(genre1:Int,genre2:Int):Double{
        val genre1String=genre1.toString()
        val genre2String=genre2.toString()
        var value=0.0
        for(i in 0..6){
            if(i<3) value+=abs(genre1String[i]-genre2String[i])
            else value+=0.667*abs(genre1String[i]-genre2String[i])
        }
        return value
    }

}