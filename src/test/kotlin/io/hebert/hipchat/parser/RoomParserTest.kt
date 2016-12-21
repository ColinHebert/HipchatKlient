package io.hebert.hipchat.parser

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import io.hebert.hipchat.api.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.reflect.KClass

class RoomParserTest : Spek({
    fun deserializerGenerator(deserializers: Map<KClass<out Any>, JsonDeserializer<out Any>>): Gson {
        return deserializers.toList().fold(GsonBuilder(), { builder, (clazz, deserializer) ->
            builder.registerTypeAdapter(clazz.java, deserializer)
        }).create()
    }

    fun serializerGenerator(serializers: Map<KClass<out Any>, JsonSerializer<out Any>>): Gson {
        return serializers.toList().fold(GsonBuilder(), { builder, (clazz, serializer) ->
            builder.registerTypeAdapter(clazz.java, serializer)
        }).create()
    }

    val gson = GsonBuilder()
            .registerTypeAdapter<RoomPrivacy> {
                serialize { (roomPrivacy, _, _) ->
                    roomPrivacy.name.toLowerCase().toJson()
                }
            }
            .registerTypeAdapter<PartialRoom> {
                serialize { (partialRoom, _, context) ->
                    JsonObject().apply {
                        add("name", partialRoom.name.toJson())
                        add("links", JsonObject().apply {
                            add("self", string().invoke().toJson())
                            add("webhooks", string().invoke().toJson())
                            val members = nullable(string()).invoke()
                            if (members != null)
                                add("members", members.toJson())
                            add("participants", string().invoke().toJson())
                        })
                        add("privacy", context.serialize(partialRoom.privacy))
                        add("is_archived", partialRoom.archived.toJson())
                        add("version", partialRoom.version.toJson())
                        add("id", partialRoom.id.toJson())
                    }
                }
            }
            .registerTypeAdapter<RoomList> {
                serialize { (roomList, _, context) ->
                    JsonObject().apply {
                        add("items", context.serialize(roomList.items))
                        add("startIndex", roomList.startIndex.toJson())
                        add("maxResults", roomList.maxResults.toJson())
                        add("links", JsonObject().apply {
                            add("self", string().invoke().toJson())
                            val next = roomList.next
                            if (next != null)
                                add("next", next.toJson())
                            val previous = roomList.previous
                            if (previous != null)
                                add("prev", previous.toJson())
                        })
                    }
                }
            }
            .registerTypeAdapter<Room> {
                serialize { (room, _, context) ->
                    JsonObject().apply {
                        add("xmpp_jid", room.xmppJid.toJson())
                        add("statistics", JsonObject().apply {
                            add("links", JsonObject().apply {
                                add("self", room.statisticsLink.toJson())
                            })
                        })
                        add("name", room.name.toJson())
                        add("links", JsonObject().apply {
                            add("self", string().invoke().toJson())
                            add("webhooks", room.webhooksLink.toJson())
                            val membersLink = room.membersLink
                            if (membersLink != null)
                                add("members", membersLink.toJson())
                            add("participants", room.participantsLink.toJson())
                        })
                        add("created", room.created.toString().toJson())
                        add("is_archived", room.archived.toJson())
                        add("privacy", context.serialize(room.privacy))
                        add("is_guest_accessible", room.guestAccessible.toJson())
                        val avatarUrl = room.avatarUrl
                        if (avatarUrl != null) {
                            add("avatar_url", avatarUrl.toJson())
                        }
                        val delegateAdminVisibility = room.delegateAdminVisibility
                        if (delegateAdminVisibility != null)
                            add("delegate_admin_visibility", delegateAdminVisibility.toJson())
                        add("topic", room.topic.toJson())
                        add("participants", context.serialize(room.participants))
                        add("version", room.version.toJson())
                        add("owner", context.serialize(room.owner))
                        add("id", room.id.toJson())
                        val guestAccessUrl = room.guestAccessUrl
                        if (guestAccessUrl != null)
                            add("guest_access_url", guestAccessUrl.toJson())
                    }
                }
            }
            .registerTypeAdapter<PartialUser> {
                serialize { (partialUser, _, _) ->
                    JsonObject().apply {
                        add("mention_name", partialUser.mentionName.toJson())
                        add("version", partialUser.version.toJson())
                        add("id", partialUser.id.toJson())
                        add("links", JsonObject().apply {
                            add("self", partialUser.userLink.toJson())
                        })
                        add("name", partialUser.name.toJson())
                    }
                }
            }
            .registerTypeAdapter<Icon> {
                deserialize { (it, _, _) ->
                    val icon = it.obj
                    Icon(
                            url = icon["url"].string,
                            urlAt2x = icon["url@2x"].nullString
                    )
                }
            }
            .registerTypeAdapter<Message> {
                deserialize { (it, _, _) ->
                    val message = it.obj
                    Message(
                            message = message["message"].string
                    )
                }
            }
            .create()!!

    describe("a room privacy deserializer") {
        val deserializer = deserializerGenerator(mapOf(RoomPrivacy::class to RoomParser.roomPrivacyDeserializer))

        RoomPrivacy.values().forEach { roomPrivacy ->
            it("should return '${roomPrivacy.name}' for the value '${roomPrivacy.name.toLowerCase()}'") {
                val deserialized = deserializer.fromJson<RoomPrivacy>(gson.toJson(roomPrivacy))
                assertThat(deserialized, equalTo(roomPrivacy))
            }
        }

        list(string()).invoke().forEach { roomPrivacy ->
            it("should fail for random value '$roomPrivacy'") {
                val actual: () -> Unit = { deserializer.fromJson<RoomPrivacy>(gson.toJson(roomPrivacy)) }
                assertThat(actual, throws(isA<IllegalArgumentException>()))
            }
        }
    }

    describe("a partial room deserializer") {
        val deserializer = deserializerGenerator(mapOf(
                RoomPrivacy::class to RoomParser.roomPrivacyDeserializer,
                PartialRoom::class to RoomParser.partialRoomDeserializer
        ))

        list(partialRoom()).invoke().forEach { partialRoom ->
            it("should parse a full version of the object $partialRoom") {
                val deserialized = deserializer.fromJson<PartialRoom>(gson.toJson(partialRoom))
                assertThat(deserialized, equalTo(partialRoom))
            }
        }
    }

    describe("a room list deserializer") {
        val deserializer = deserializerGenerator(mapOf(
                RoomPrivacy::class to RoomParser.roomPrivacyDeserializer,
                PartialRoom::class to RoomParser.partialRoomDeserializer,
                RoomList::class to RoomParser.roomListDeserializer
        ))

        list(roomList()).invoke().forEach { roomList ->
            it("should parse a full version of the object $roomList") {
                val deserialized = deserializer.fromJson<RoomList>(gson.toJson(roomList))
                assertThat(deserialized, equalTo(roomList))
            }
        }
    }

    describe("a room deserializer") {
        val deserializer = deserializerGenerator(mapOf(
                RoomPrivacy::class to RoomParser.roomPrivacyDeserializer,
                Room::class to RoomParser.roomDeserializer,
                PartialUser::class to UserParser.partialUserDeserializer
        ))

        list(room()).invoke().forEach { room ->
            it("should parse a full version of the object $room") {
                val deserialized = deserializer.fromJson<Room>(gson.toJson(room))
                assertThat(deserialized, equalTo(room))
            }
        }
    }

    describe("an icon serializer") {
        val serializer = serializerGenerator(mapOf(
                Icon::class to RoomParser.iconSerializer
        ))

        list(icon()).invoke().forEach { icon ->
            it("should parse a full version of the object $icon") {
                val deserialized = gson.fromJson<Icon>(serializer.toJson(icon))
                assertThat(deserialized, equalTo(icon))
            }
        }
    }

    describe("an message serializer") {
        val serializer = serializerGenerator(mapOf(
                Message::class to RoomParser.messageSerializer
        ))

        list(message()).invoke().forEach { message ->
            it("should parse a full version of the object $message") {
                val deserialized = gson.fromJson<Message>(serializer.toJson(message))
                assertThat(deserialized, equalTo(message))
            }
        }
    }

    describe("an description serializer") {
        val serializer = serializerGenerator(mapOf(
                Notification.Card.Description::class to RoomParser.notificationCardDescriptionSerializer
        ))

        list(description()).invoke().forEach { description ->
            it("should parse a full version of the object $description") {
                val deserialized = gson.fromJson<Notification.Card.Description>(serializer.toJson(description))
                assertThat(deserialized, equalTo(description))
            }
        }
    }
})
