package io.hebert.hipchat.parser

import io.hebert.hipchat.api.*
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.zone.ZoneRulesProvider


fun partialRoom() = {
    PartialRoom(
            name = string().invoke(),
            privacy = oneOf<RoomPrivacy>().invoke(),
            archived = boolean().invoke(),
            version = string().invoke(),
            id = int().invoke()
    )
}

fun roomList() = {
    RoomList(
            items = list(partialRoom()).invoke(),
            maxResults = int().invoke(),
            next = nullable(string()).invoke(),
            previous = nullable(string()).invoke(),
            startIndex = int().invoke()
    )
}

fun instant() = {
    Instant.ofEpochMilli(long().invoke())
}

fun zone() = {
    ZoneId.of(oneOf(ZoneRulesProvider.getAvailableZoneIds().toTypedArray()).invoke())
}

fun partialUser() = {
    PartialUser(
            id = int().invoke(),
            mentionName = string().invoke(),
            name = string().invoke(),
            userLink = string().invoke(),
            version = string().invoke()
    )
}

fun room() = {
    Room(
            name = string().invoke(),
            xmppJid = string().invoke(),
            created = OffsetDateTime.ofInstant(instant().invoke(), zone().invoke()),
            archived = boolean().invoke(),
            privacy = oneOf<RoomPrivacy>().invoke(),
            guestAccessible = boolean().invoke(),
            avatarUrl = nullable(string()).invoke(),
            delegateAdminVisibility = nullable(boolean()).invoke(),
            topic = string().invoke(),
            participants = list(partialUser()).invoke(),
            version = string().invoke(),
            owner = partialUser().invoke(),
            id = int().invoke(),
            guestAccessUrl = nullable(string()).invoke(),
            statisticsLink = string().invoke(),
            webhooksLink = string().invoke(),
            membersLink = nullable(string()).invoke(),
            participantsLink = string().invoke()
    )
}

fun icon() = {
    Icon(
            url = string().invoke(),
            urlAt2x = nullable(string()).invoke()
    )
}

fun message() = {
    Message(
            message = string().invoke()
    )
}

fun description() = {
    Notification.Card.Description(
            value = string().invoke(),
            format = oneOf<MessageFormat>().invoke()
    )
}

fun thumbnail() = {
    Notification.Card.Thumbnail(
            url = string().invoke(),
            width = nullable(int()).invoke(),
            urlAt2x = nullable(string()).invoke(),
            height = nullable(int()).invoke()
    )
}

fun activity() = {
    Notification.Card.Activity(
            html = string().invoke(),
            icon = nullable(icon()).invoke()
    )
}

fun value() = {
    Notification.Card.Attribute.Value(
            url = nullable(string()).invoke(),
            style = nullable(oneOf<Notification.Card.Attribute.Value.Style>()).invoke(),
            label = string().invoke(),
            icon = nullable(icon()).invoke()
    )
}

fun attribute() = {
    Notification.Card.Attribute(
            value = value().invoke(),
            label = nullable(string()).invoke()
    )
}

fun card() = {
    Notification.Card(
            style = oneOf<Notification.Card.Style>().invoke(),
            description = nullable(description()).invoke(),
            format = nullable(oneOf<Notification.Card.Format>()).invoke(),
            url = nullable(string()).invoke(),
            title = string().invoke(),
            thumbnail = thumbnail().invoke(),
            activity = activity().invoke(),
            attributes = list(attribute()).invoke(),
            id = string().invoke(),
            icon = nullable(icon()).invoke()
    )
}

fun notification() = {
    Notification(
            from = nullable(string()).invoke(),
            messageFormat = nullable(oneOf<MessageFormat>()).invoke(),
            color = nullable(oneOf<Notification.Color>()).invoke(),
            attachTo = nullable(string()).invoke(),
            notify = nullable(boolean()).invoke(),
            message = string().invoke(),
            card = card().invoke()
    )
}