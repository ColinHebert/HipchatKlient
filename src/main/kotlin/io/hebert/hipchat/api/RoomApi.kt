@file:JvmName("HipchatRoomAPI")

package io.hebert.hipchat.api

import io.hebert.hipchat.HipchatClient
import io.hebert.hipchat.parser.RoomParser
import java.time.OffsetDateTime

enum class RoomPrivacy { PUBLIC, PRIVATE}
data class PartialRoom(val name: String, val privacy: RoomPrivacy, val archived: Boolean, val version: String, val id: Int)
data class RoomList(val items: List<PartialRoom>, val startIndex: Int, val maxResults: Int, val previous: String?, val next: String?)

data class Room(val name: String, val xmppJid: String, val created: OffsetDateTime, val archived: Boolean,
                val privacy: RoomPrivacy, val guestAccessible: Boolean, val avatarUrl: String?, val delegateAdminVisibility: Boolean?,
                val topic: String, val participants: List<PartialUser>, val version: String, val owner: PartialUser, val id: Int,
                val guestAccessUrl: String?, val statisticsLink: String, val webhooksLink: String, val membersLink: String?, val participantsLink: String)

enum class MessageFormat { HTML, TEXT }
data class Icon(val url: String, val urlAt2x: String? = null)

data class Message(val message: String)

data class Notification(val from: String? = null, val messageFormat: MessageFormat? = null, val color: Color? = null, val attachTo: String? = null,
                        val notify: Boolean? = null, val message: String, val card: Card? = null) {
    enum class Color { YELLOW, GREEN, RED, PURPLE, GRAY, RANDOM }
    data class Card(val style: Style, val description: Description? = null, val format: Format? = null, val url: String? = null,
                    val title: String, val thumbnail: Thumbnail? = null, val activity: Activity? = null,
                    val attributes: List<Attribute> = emptyList(), val id: String, val icon: Icon? = null) {
        enum class Style { FILE, IMAGE, APPLICATION, LINK, MEDIA }
        data class Description(val value: String, val format: MessageFormat)
        enum class Format { COMPACT, MEDIUM }
        data class Activity(val html: String, val icon: Icon? = null)
        data class Thumbnail(val url: String, val width: Int? = null, val urlAt2x: String? = null, val height: Int? = null)
        data class Attribute(val label: String? = null, val value: Value) {
            data class Value(val url: String? = null, val style: Style? = null, val label: String, val icon: Icon? = null) {
                enum class Style { LOZENGE_SUCCESS, LOZENGE_ERROR, LOZENGE_CURRENT, LOZENGE_COMPLETE, LOZENGE_MOVED, LOZENGE }
            }
        }
    }
}

@JvmOverloads
fun HipchatClient.getAllRooms(startIndex: Int? = null, maxResults: Int? = null, includePrivate: Boolean? = null, includeArchived: Boolean? = null): RoomList {
    val parameters = mapOf(
            "start-index" to startIndex,
            "max-results" to maxResults,
            "include-private" to includePrivate,
            "include-archived" to includeArchived).mapNotNull {
        if (it.value != null)
            it.key to listOf(it.value.toString())
        else
            null
    }.toMap()

    return runAgainstApi(
            path = "/v2/room",
            parameters = parameters.mapValues { it.value },
            lambda = { RoomParser.builder.fromJson(it.entity.content.reader(), RoomList::class.java) }
    )
}

fun HipchatClient.getRoom(idOrName: String): Room? {
    return runAgainstApi(
            path = "/v2/room/$idOrName",
            lambda = {
                if (it.statusLine.statusCode != 404) RoomParser.builder.fromJson(it.entity.content.reader(), Room::class.java) else null
            }
    )
}

fun HipchatClient.getRoom(idOrName: Int): Room? {
    return getRoom(idOrName.toString())
}

fun HipchatClient.sendMessage(roomIdOrName: String, message: Message) {
    return runAgainstApi(
            path = "/v2/room/$roomIdOrName/message",
            request = HipchatClient.RequestType.POST,
            content = RoomParser.builder.toJson(message),
            lambda = {
                println(it.statusLine.statusCode)
                println(it.statusLine.reasonPhrase)
            }
    )
}

fun HipchatClient.sendMessage(roomIdOrName: Int, message: Message) {
    return sendMessage(roomIdOrName.toString(), message)
}

fun HipchatClient.sendNotification(roomIdOrName: String, notification: Notification) {
    HipchatClient.logger.debug { "Sending a notification to $roomIdOrName" }
    return runAgainstApi(
            path = "/v2/room/$roomIdOrName/notification",
            request = HipchatClient.RequestType.POST,
            content = RoomParser.builder.toJson(notification),
            lambda = {
            }
    )
}

fun HipchatClient.sendNotification(roomIdOrName: Int, notification: Notification) {
    return sendNotification(roomIdOrName.toString(), notification)
}
