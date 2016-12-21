@file:JvmName("HipchatUserAPI")

package io.hebert.hipchat.api

import io.hebert.hipchat.HipchatKlient
import io.hebert.hipchat.parser.UserParser
import java.time.OffsetDateTime
import java.util.*

data class PartialUser(val id: Int, val mentionName: String, val name: String, val version: String, val userLink: String)
data class UserList(val items: List<PartialUser>, val startIndex: Int, val maxResults: Int, val previous: String?, val next: String?)

data class User(val name: String, val xmppJid: String, val deleted: Boolean, val lastActive: OffsetDateTime?,
                val title: String, val presence: Presence?, val created: OffsetDateTime, val id: Int,
                val mentionName: String, val version: String, val roles: List<String>, val groupAdmin: Boolean,
                val timezone: TimeZone, val guest: Boolean, val email: String?, val photoUrl: String?) {
    data class Presence(val status: String?, val idle: Int?, val show: Show?, val client: Client?, val online: Boolean) {
        enum class Show { AWAY, CHAT, DND, XA }
        data class Client(val version: String?, val type: String?)
    }
}

@JvmOverloads
fun HipchatKlient.getAllUsers(startIndex: Int? = null, maxResults: Int? = null, includeGuests: Boolean? = null, includeDeleted: Boolean? = null): UserList {
    val parameters = mapOf(
            "start-index" to startIndex,
            "max-results" to maxResults,
            "include-guests" to includeGuests,
            "include-deleted" to includeDeleted).mapNotNull {
        if (it.value != null)
            it.key to listOf(it.value.toString())
        else
            null
    }.toMap()

    return runAgainstApi(
            path = "/v2/user",
            parameters = parameters,
            lambda = { UserParser.builder.fromJson(it.entity.content.reader(), UserList::class.java) }
    )
}

fun HipchatKlient.getUser(idOrEmail: String): User? {
    return runAgainstApi(
            path = "/v2/user/$idOrEmail",
            lambda = { if (it.statusLine.statusCode != 404) UserParser.builder.fromJson(it.entity.content.reader(), User::class.java) else null }
    )
}

fun HipchatKlient.getUser(idOrEmail: Int): User? {
    return getUser(idOrEmail.toString())
}

