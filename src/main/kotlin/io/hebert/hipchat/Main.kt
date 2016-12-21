package io.hebert.hipchat

import io.hebert.hipchat.api.*
import io.hebert.hipchat.helper.sendNotification

fun main(args: Array<String>) {
    HipchatKlient("token").let {
        it.getRoom("Colin Test")?.sendNotification(
                client = it,
                notification = Notification(
                        message = "test_colin",
                        from = "Totally Not Colin!",
                        color = Notification.Color.GRAY,
                        card = Notification.Card(
                                id = "test",
                                style = Notification.Card.Style.APPLICATION,
                                title = "Hello!",
                                url = "http://google.com",
                                format = Notification.Card.Format.MEDIUM,
                                description = Notification.Card.Description(
                                        format = MessageFormat.TEXT,
                                        value = "<b>description</b> BTW the description is here look also @Colin are you gettin a notification? at all that content I wonder what happens when a very long line is provided though. Does the window get slightly larger? Or does it keep the size it has by default?"
                                ),
                                activity = Notification.Card.Activity(
                                        html = "<i>activity</i> just some text, perfectly normal",
                                        icon = Icon("https://b.thumbs.redditmedia.com/WGLVgONUiLstTGyCzp1_p4FkTr2I3e5zIiA4KrBxYmg.jpg")
                                ),
                                thumbnail = Notification.Card.Thumbnail(
                                        url = "https://b.thumbs.redditmedia.com/XN-aXFRLlZV_JDtivaVgc6qAVWJDPMlQ3fqhUzvOBmI.jpg"
                                ),
                                attributes = listOf(Notification.Card.Attribute(
                                        label = "label!",
                                        value = Notification.Card.Attribute.Value(
                                                url = "http://free.fr",
                                                label = "myLabel!!!!!",
                                                style = Notification.Card.Attribute.Value.Style.LOZENGE,
                                                icon = Icon("https://b.thumbs.redditmedia.com/fOWbUHgVRuSB3ZiakqTQr_itgbZ119XHUlSc78v7JNk.jpg")
                                        )
                                )),
                                icon = Icon("https://b.thumbs.redditmedia.com/Whc5XQ0Pob7mDL4BHRwYXgkQnyodO767j_aFKB3KuYs.jpg")
                        )
                )
        )
    }
}