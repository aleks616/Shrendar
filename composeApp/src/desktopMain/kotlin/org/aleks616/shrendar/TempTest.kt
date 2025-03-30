package org.aleks616.shrendar
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

fun main(){
    runBlocking{
        val client=HttpClient(CIO)
        val response=client.get("http://127.0.0.1:8081/api/ranks")
        println(response.body<String>())
    }
}