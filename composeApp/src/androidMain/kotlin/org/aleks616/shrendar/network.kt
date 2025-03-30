import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*

actual class NetworkClient actual constructor() {
    actual suspend fun fetchRanks():List<Ranks> {
        val response=js("fetch('http://localhost:8081/api/ranks')").unsafeCast<Promise<Response>>()
        val text=response.await().text().await().unsafeCast<String>()
        return Json.decodeFromString(text)
    }
}