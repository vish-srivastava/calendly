package com.calendy.services

import com.calendy.models.Message
import com.calendy.models.NotificationType
import com.calendy.models.User
import org.springframework.stereotype.Service

@Service
class NotificationEngine {

    fun notifyUsers(guests: List<User>, message: Message, notificationTypes: List<NotificationType>) {

    }

    fun sendEmail(user: User, message: Message) {

    }

    fun sendSms(user: User, message: Message) {

    }

    fun sendPushNotification(user: User, message: Message) {

    }

    fun callViaIVR(user: User, message: Message) {

    }

}