package io.hebert.hipchat.parser

import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import io.hebert.hipchat.api.PartialUser
import io.hebert.hipchat.api.User
import io.hebert.hipchat.api.UserList
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object UserParser {
    val builder by lazy {
        GsonBuilder()
                .registerTypeAdapter<PartialUser>(partialUserDeserializer)
                .registerTypeAdapter<UserList>(userListDeserializer)
                .registerTypeAdapter<User.Presence.Show>(presenceShowDeserializer)
                .registerTypeAdapter<User.Presence.Client>(presenceClientDeserializer)
                .registerTypeAdapter<User.Presence>(presenceDeserializer)
                .registerTypeAdapter<User>(userDeserializer)
                .create()!!
    }

    val partialUserDeserializer by lazy {
        jsonDeserializer { (it, _, _) ->
            val partialUser = it.obj
            PartialUser(
                    id = partialUser["id"].int,
                    mentionName = partialUser["mention_name"].string,
                    name = partialUser["name"].string,
                    version = partialUser["version"].string,
                    userLink = partialUser["links"].obj["self"].string
            )
        }
    }

    val userListDeserializer by lazy {
        jsonDeserializer { (it, _, context) ->
            val userList = it.obj
            UserList(
                    items = userList["items"].array.map { context.deserialize<PartialUser>(it) },
                    startIndex = userList["startIndex"].int,
                    maxResults = userList["maxResults"].int,
                    previous = userList["links"].obj["prev"]?.string,
                    next = userList["links"].obj["next"]?.string
            )
        }
    }

    val presenceShowDeserializer by lazy {
        jsonDeserializer { (it, _, _) ->
            val presenceShow = it.string
            User.Presence.Show.valueOf(presenceShow.toUpperCase())
        }
    }

    val presenceClientDeserializer by lazy {
        jsonDeserializer { (it, _, _) ->
            val presenceClient = it.obj
            User.Presence.Client(
                    version = presenceClient["version"].nullString,
                    type = presenceClient["type"].nullString
            )
        }
    }

    val presenceDeserializer by lazy {
        jsonDeserializer {(it, _, context) ->
            val presence = it.obj
            User.Presence(
                    status = presence["status"].nullString,
                    idle = presence["idle"].nullInt,
                    show = presence["show"]?.let { context.deserialize<User.Presence.Show>(it) },
                    client = presence["client"]?.let { context.deserialize<User.Presence.Client>(it) },
                    online = presence["is_online"].bool
            )
        }
    }

    val userDeserializer by lazy {
        jsonDeserializer { (it, _, context) ->
            val user = it.obj
            User(
                    name = user["name"].string,
                    xmppJid = user["xmpp_jid"].string,
                    deleted = user["is_deleted"].bool,
                    lastActive = user["last_active"]?.let { OffsetDateTime.parse(it.string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX")) },
                    title = user["title"].string,
                    presence = user["presence"]?.let { context.deserialize<User.Presence>(it) },
                    created = user["created"].let { OffsetDateTime.parse(it.string, DateTimeFormatter.ISO_OFFSET_DATE_TIME) },
                    id = user["id"].int,
                    mentionName = user["mention_name"].string,
                    version = user["version"].string,
                    roles = user["roles"].array.map { it.string },
                    groupAdmin = user["is_group_admin"].bool,
                    timezone = TimeZone.getTimeZone(user["timezone"].string),
                    guest = user["is_guest"].bool,
                    email = user["email"].nullString,
                    photoUrl = user["photo_url"].nullString
            )
        }
    }
}