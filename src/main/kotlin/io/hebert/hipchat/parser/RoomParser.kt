package io.hebert.hipchat.parser

import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.hebert.hipchat.api.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object RoomParser {
    val builder by lazy {
        GsonBuilder()
                .registerTypeAdapter<RoomPrivacy>(roomPrivacyDeserializer)
                .registerTypeAdapter<PartialRoom>(partialRoomDeserializer)
                .registerTypeAdapter<RoomList>(roomListDeserializer)
                .registerTypeAdapter<PartialUser>(UserParser.partialUserDeserializer)
                .registerTypeAdapter<Room>(roomDeserializer)
                .registerTypeAdapter<MessageFormat>(messageFormatSerializer)
                .registerTypeAdapter<Icon>(iconSerializer)
                .registerTypeAdapter<Message>(messageSerializer)
                .registerTypeAdapter<Notification>(notificationSerializer)
                .registerTypeAdapter<Notification.Color>(notificationColorSerializer)
                .registerTypeAdapter<Notification.Card>(notificationCardSerializer)
                .registerTypeAdapter<Notification.Card.Style>(notificationCardStyleSerializer)
                .registerTypeAdapter<Notification.Card.Description>(notificationCardDescriptionSerializer)
                .registerTypeAdapter<Notification.Card.Format>(notificationCardFormatSerializer)
                .registerTypeAdapter<Notification.Card.Activity>(notificationCardActivitySerializer)
                .registerTypeAdapter<Notification.Card.Thumbnail>(notificationCardThumbnailSerializer)
                .registerTypeAdapter<Notification.Card.Attribute>(notificationCardAttributeSerializer)
                .registerTypeAdapter<Notification.Card.Attribute.Value>(notificationCardAttributeValueSerializer)
                .registerTypeAdapter<Notification.Card.Attribute.Value.Style>(notificationCardAttributeValueStyleSerializer)
                .create()!!
    }

    val roomPrivacyDeserializer by lazy {
        jsonDeserializer { (it, _, _) ->
            val roomPrivacy = it.string
            RoomPrivacy.valueOf(roomPrivacy.toUpperCase())
        }
    }

    val partialRoomDeserializer by lazy {
        jsonDeserializer { (it, _, context) ->
            val partialRoom = it.obj
            PartialRoom(
                    name = partialRoom["name"].string,
                    privacy = partialRoom["privacy"].let { context.deserialize<RoomPrivacy>(it) },
                    archived = partialRoom["is_archived"].bool,
                    version = partialRoom["version"].string,
                    id = partialRoom["id"].int
            )
        }
    }

    val roomListDeserializer by lazy {
        jsonDeserializer { (it, _, context) ->
            val roomList = it.obj
            RoomList(
                    items = roomList["items"].array.map { context.deserialize<PartialRoom>(it) },
                    startIndex = roomList["startIndex"].int,
                    maxResults = roomList["maxResults"].int,
                    previous = roomList["links"].obj["prev"]?.asString,
                    next = roomList["links"].obj["next"]?.asString
            )
        }
    }

    val roomDeserializer by lazy {
        jsonDeserializer { (it, _, context) ->
            val room = it.obj
            Room(
                    xmppJid = room["xmpp_jid"].string,
                    statisticsLink = room["statistics"].obj["links"].obj["self"].string,
                    name = room["name"].string,
                    webhooksLink = room["links"].obj["webhooks"].string,
                    membersLink = room["links"].obj["members"].nullString,
                    participantsLink = room["links"].obj["participants"].string,
                    created = room["created"].let { OffsetDateTime.parse(it.string, DateTimeFormatter.ISO_OFFSET_DATE_TIME) },
                    archived = room["is_archived"].bool,
                    privacy = room["privacy"].let { context.deserialize<RoomPrivacy>(it) },
                    guestAccessible = room["is_guest_accessible"].bool,
                    avatarUrl = room["avatar_url"].nullString,
                    delegateAdminVisibility = room["delegate_admin_visibility"].nullBool,
                    topic = room["topic"].string,
                    participants = room["participants"].array.map { context.deserialize<PartialUser>(it) },
                    version = room["version"].string,
                    owner = room["owner"].let { context.deserialize<PartialUser>(it) },
                    id = room["id"].int,
                    guestAccessUrl = room["guest_access_url"].nullString
            )
        }
    }

    val iconSerializer by lazy {
        jsonSerializer<Icon> { (icon, _, _) ->
            JsonObject().apply {
                add("url", icon.url.toJson())
                if (icon.urlAt2x != null)
                    add("url@2x", icon.urlAt2x.toJson())
            }
        }
    }

    val messageSerializer by lazy {
        jsonSerializer<Message> { (message, _, _) ->
            JsonObject().apply {
                add("message", message.message.toJson())
            }
        }
    }

    val notificationSerializer by lazy {
        jsonSerializer<Notification> { (notification, _, context) ->
            JsonObject().apply {
                if (notification.from != null)
                    add("from", notification.from.toJson())
                if (notification.messageFormat != null)
                    add("message_format", context.serialize(notification.messageFormat))
                if (notification.color != null)
                    add("color", context.serialize(notification.color))
                if (notification.attachTo != null)
                    add("attach_to", notification.attachTo.toJson())
                if (notification.notify != null)
                    add("notify", notification.notify.toJson())
                add("message", notification.message.toJson())
                if (notification.card != null)
                    add("card", context.serialize(notification.card))
            }
        }
    }

    val notificationColorSerializer by lazy {
        jsonSerializer<Notification.Color> { (color, _, _) ->
            color.name.toLowerCase().toJson()
        }
    }

    val messageFormatSerializer by lazy {
        jsonSerializer<MessageFormat> { (messageFormat, _, _) ->
            messageFormat.name.toLowerCase().toJson()
        }
    }

    val notificationCardSerializer by lazy {
        jsonSerializer<Notification.Card> { (card, _, context) ->
            JsonObject().apply {
                add("style", context.serialize(card.style))
                if (card.description != null)
                    add("description", context.serialize(card.description))
                if (card.format != null)
                    add("format", context.serialize(card.format))
                if (card.url != null)
                    add("url", card.url.toJson())
                add("title", card.title.toJson())
                if (card.thumbnail != null)
                    add("thumbnail", context.serialize(card.thumbnail))
                if (card.activity != null)
                    add("activity", context.serialize(card.activity))
                if (card.attributes.isNotEmpty())
                    add("attributes", jsonArray(card.attributes.map { context.serialize(it) }))
                add("id", card.id.toJson())
                if (card.icon != null)
                    add("icon", context.serialize(card.icon))
            }
        }
    }

    val notificationCardStyleSerializer by lazy {
        jsonSerializer<Notification.Card.Style> { (style, _, _) ->
            style.name.toLowerCase().toJson()
        }
    }

    val notificationCardFormatSerializer by lazy {
        jsonSerializer<Notification.Card.Format> { (format, _, _) ->
            format.name.toLowerCase().toJson()
        }
    }

    val notificationCardDescriptionSerializer by lazy {
        jsonSerializer<Notification.Card.Description> { (description, _, context) ->
            JsonObject().apply {
                add("value", description.value.toJson())
                add("format", context.serialize(description.format))
            }
        }
    }

    val notificationCardThumbnailSerializer by lazy {
        jsonSerializer<Notification.Card.Thumbnail> { (thumbnail, _, _) ->
            JsonObject().apply {
                add("url", thumbnail.url.toJson())
                if (thumbnail.width != null)
                    add("width", thumbnail.width.toJson())
                if (thumbnail.urlAt2x != null)
                    add("url@2x", thumbnail.urlAt2x.toJson())
                if (thumbnail.height != null)
                    add("height", thumbnail.height.toJson())
            }
        }
    }

    val notificationCardActivitySerializer by lazy {
        jsonSerializer<Notification.Card.Activity> { (activity, _, context) ->
            JsonObject().apply {
                add("html", activity.html.toJson())
                if (activity.icon != null)
                    add("icon", context.serialize(activity.icon))
            }
        }
    }

    val notificationCardAttributeSerializer by lazy {
        jsonSerializer<Notification.Card.Attribute> { (attribute, _, context) ->
            JsonObject().apply {
                add("value", context.serialize(attribute.value))
                if (attribute.label != null)
                    add("label", attribute.label.toJson())
            }
        }
    }

    val notificationCardAttributeValueSerializer by lazy {
        jsonSerializer<Notification.Card.Attribute.Value> { (value, _, context) ->
            JsonObject().apply {
                if (value.url != null)
                    add("url", value.url.toJson())
                add("style", context.serialize(value.style))
                add("label", value.label.toJson())
                if (value.icon != null)
                    add("icon", context.serialize(value.icon))
            }
        }
    }

    val notificationCardAttributeValueStyleSerializer by lazy {
        jsonSerializer<Notification.Card.Attribute.Value.Style> { (format, _, _) ->
            format.name.toLowerCase().replace('_', '-').toJson()
        }
    }
}
