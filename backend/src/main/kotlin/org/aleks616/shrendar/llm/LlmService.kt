package org.aleks616.shrendar.llm

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class LlmService {

    @Value("\${gemini.api.url}")
    private val apiUrl:String?=null

    @Value("\${gemini.api.key}")
    private val apiKey:String?=null

    private val restTemplate=RestTemplate()

    fun translate(request:TranslationRequestDto):String {
        val prompt="Translate this description into ${request.targetLanguage}. Return only the translation. Preserve the original meaning, facts, tone, formatting, names, and line breaks. Do not summarize, rewrite, explain, add, or remove information.\n\n${request.text}"
        val requestBody=GeminiDTO.Request(prompt)

        try {
            val key=apiKey?.takeUnless {it.isBlank()}
                    ?:throw IllegalStateException("Gemini API key is not configured")

            val headers=HttpHeaders().apply {
                contentType=MediaType.APPLICATION_JSON
                set("X-goog-api-key",key)
            }
            val response=restTemplate.exchange(
                apiUrl!!,
                HttpMethod.POST,
                HttpEntity(requestBody,headers),
                Map::class.java
            )

            val responseBody=response.body
            val candidates=responseBody?.get("candidates") as? List<*>
            val firstCandidate=candidates?.firstOrNull() as? Map<*,*>
            val content=firstCandidate?.get("content") as? Map<*,*>
            val parts=content?.get("parts") as? List<*>
            val firstPart=parts?.firstOrNull() as? Map<*,*>
            val translation=firstPart?.get("text") as? String

            if(translation.isNullOrEmpty()) {
                return "no translation"
            }

            return translation
        }
        catch(e:Exception) {
            println(e.message)
            return "no translation"
        }
    }

}
