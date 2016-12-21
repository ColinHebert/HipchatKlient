@file:JvmName("HipchatUserHelper")

package io.hebert.hipchat.helper

import io.hebert.hipchat.HipchatKlient
import io.hebert.hipchat.api.*


fun UserList.getPreviousPage(client: HipchatKlient): UserList? {
    if (previous == null)
        return null
    val (_, parameters) = client.parseUrl(previous)

    return client.getAllUsers(
            startIndex = parameters["start-index"]?.firstOrNull()?.toInt(),
            maxResults = parameters["max-results"]?.firstOrNull()?.toInt(),
            includeGuests = parameters["include-guests"]?.firstOrNull()?.toBoolean(),
            includeDeleted = parameters["include-deleted"]?.firstOrNull()?.toBoolean()
    )
}

fun UserList.getNextPage(client: HipchatKlient): UserList? {
    if (next == null)
        return null
    val (_, parameters) = client.parseUrl(next)

    return client.getAllUsers(
            startIndex = parameters["start-index"]?.firstOrNull()?.toInt(),
            maxResults = parameters["max-results"]?.firstOrNull()?.toInt(),
            includeGuests = parameters["include-guests"]?.firstOrNull()?.toBoolean(),
            includeDeleted = parameters["include-deleted"]?.firstOrNull()?.toBoolean()
    )
}

fun PartialUser.getFullUser(client: HipchatKlient): User? {
    return client.getUser(id)
}