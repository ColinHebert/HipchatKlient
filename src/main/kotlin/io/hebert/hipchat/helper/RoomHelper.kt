@file:JvmName("HipchatRoomHelper")

package io.hebert.hipchat.helper

import io.hebert.hipchat.HipchatClient
import io.hebert.hipchat.api.*

fun RoomList.getPreviousPage(client: HipchatClient): RoomList? {
    if (previous == null)
        return null
    val (_, parameters) = client.parseUrl(previous)

    return client.getAllRooms(
            startIndex = parameters["start-index"]?.firstOrNull()?.toInt(),
            maxResults = parameters["max-results"]?.firstOrNull()?.toInt(),
            includeArchived = parameters["include-archived"]?.firstOrNull()?.toBoolean(),
            includePrivate = parameters["include-private"]?.firstOrNull()?.toBoolean()
    )
}

fun RoomList.getNextPage(client: HipchatClient): RoomList? {
    if (next == null)
        return null
    val parameters = client.parseUrl(next).parameters

    return client.getAllRooms(
            startIndex = parameters["start-index"]?.firstOrNull()?.toInt(),
            maxResults = parameters["max-results"]?.firstOrNull()?.toInt(),
            includeArchived = parameters["include-archived"]?.firstOrNull()?.toBoolean(),
            includePrivate = parameters["include-private"]?.firstOrNull()?.toBoolean()
    )
}

fun PartialRoom.getFullRoom(client: HipchatClient): Room? {
    return client.getRoom(id)
}

fun PartialRoom.sendMessage(client: HipchatClient, message: Message){
    client.sendMessage(id, message)
}

fun PartialRoom.sendNotification(client: HipchatClient, notification: Notification){
    client.sendNotification(id, notification)
}

fun Room.sendMessage(client: HipchatClient, message: Message){
    client.sendMessage(id, message)
}

fun Room.sendNotification(client: HipchatClient, notification: Notification){
    client.sendNotification(id, notification)
}