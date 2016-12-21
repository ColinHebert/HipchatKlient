@file:JvmName("HipchatUserHelper")

package io.hebert.hipchat.helper

import io.hebert.hipchat.HipchatClient
import io.hebert.hipchat.api.*


fun UserList.getPreviousPage(client: HipchatClient): UserList? {
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

fun UserList.getNextPage(client: HipchatClient): UserList? {
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

fun PartialUser.getFullUser(client: HipchatClient): User? {
    return client.getUser(id)
}