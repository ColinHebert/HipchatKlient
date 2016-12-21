package io.hebert.hipchat

import mu.KLogging
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import java.net.URI
import java.net.URLDecoder

class HipchatClient @JvmOverloads constructor(val token: String,
                                              apiUrl: String = "https://api.hipchat.com",
                                              val httpClient: HttpClient = HttpClients.createMinimal()) {
    companion object : KLogging()

    val apiUrl = URI.create((if (!apiUrl.endsWith('/')) apiUrl else apiUrl.dropLast(1)))

    enum class RequestType { GET, POST, PUT }

    @JvmOverloads
    fun <T> runAgainstApi(path: String, request: RequestType = RequestType.GET, parameters: Map<String, List<String>> = emptyMap(), content: String = "", lambda: (HttpResponse) -> T): T {
        val queryURI = URIBuilder(apiUrl).apply {
            this.path += path
            parameters.forEach {
                val key = it.key
                val values = it.value
                if (values.isEmpty()) {
                    addParameter(key, null)
                } else {
                    values.forEach { addParameter(key, it) }
                }
            }
        }.build()

        val httpRequest = when (request) {
            HipchatClient.RequestType.GET -> HttpGet(queryURI)
            HipchatClient.RequestType.POST -> HttpPost(queryURI).apply { entity = StringEntity(content) }
            HipchatClient.RequestType.PUT -> HttpPut(queryURI).apply { entity = StringEntity(content) }
        }.apply {
            addHeader(BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"))
            addHeader(BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer $token"))
        }

        return httpClient.execute(httpRequest, {
            logger.debug { "$request \"$path\" - ${it.statusLine.statusCode}: ${it.statusLine.reasonPhrase}"}
            if (it.statusLine.statusCode in arrayOf(400)){
                logger.error { "Couldn't execute $request \"$path\"; ${it.entity.content.reader().readText()}" }
            }
            lambda(it)
        })
    }

    fun checkAuthentication(): Boolean {
        val authWorked: Boolean = runAgainstApi("/v2/room", parameters = hashMapOf("auth_test" to listOf("true")), lambda = {
            (it.statusLine.statusCode == 403) or (it.statusLine.statusCode == 202)
        })

        return authWorked
    }

    data class ParsedUrl(val path: String, val parameters: Map<String, List<String>>)

    fun parseUrl(url: String): ParsedUrl {
        val uri = URI.create(url)

        if (!url.startsWith(apiUrl.toASCIIString()))
            TODO("Throw a nice exception when the API URL doesn't match the page we want to load")

        val parameters = uri.query.orEmpty().split('&').map {
            val parameter = it.split('=', limit = 2)
            //Explicit definition of "it" as it must not be null
            val name = parameter.first().let { it: String -> URLDecoder.decode(it, "UTF-8")!! }
            val value = parameter.getOrNull(1)?.let { it: String -> URLDecoder.decode(it, "UTF-8")!! }
            Pair(name, value)
        }.fold(mapOf<String, List<String>>(), { map, parameter ->
            val parameterName: String = parameter.first
            val parameterValue: String? = parameter.second

            val currentValues = map[parameterName] ?: emptyList()
            val newValues: List<String> = if (parameterValue != null) currentValues.plus(parameterValue) else currentValues
            map.plus(Pair(parameterName, newValues))
        })

        return ParsedUrl("", parameters)
    }
}