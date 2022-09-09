package com.calendy.models

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*


@JsonAutoDetect
data class CreateUserRequest(
    val name: String,
    val email: String,
    val phone: String,
    val accountsToLink: Map<ThirdPartyCalenderType, String>
)

@Table("user")
data class User(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val linkedThirdPartyCalenders: Map<ThirdPartyCalenderType, String>
)


data class UserMetadata(
    val metadata: Map<String, String>
)

data class CalenderEvent(
    val eventId: String,
    val isActive: Boolean,
    val paymentRequired: Boolean,
    val hostUserId: String,
    val eventDetails: EventMetadata,
    val slotWindowType: SlotWindowType,
    val slotDurationMinutes: Int,
    val dailyStartTimeMins: Int,
    val dailyEndTimeMins: Int,
    val eventStartDate: Date?,
    val eventEndDate: Date?,
    val timeZone: String? = null
)

data class SlotBookingRequest(
    val eventId: String,
    val requestedSlot: Slot
)

data class Slot(
    val inviteeUserId: String,
    val slotId: String,
    val startTime: Date,
    val endTime: Date
)

data class EventMetadata(
    val eventLocation: EventLocation,
    val eventLocationUrl: String,
    val guestEmails: List<String>
)

enum class EventLocation {
    ZOOM,
    GOOGLE_MEET,
    MICROSOFT_TEAMS,
    PHONE,
    IN_PERSON
}


enum class CalenderEventType {
    CONSULTATION, INTERVIEW, MEETING
}


enum class SlotWindowType {
    ROLLING, FIXED_WINDOW
}

enum class ThirdPartyCalenderType {
    GOOGLE_CALENDAR,
    MICROSOFT_OUTLOOK,
    APPLE_CALENDAR
}

data class Message(
    val messageBody: String,
    val messageHeader: String? = "",
    val isImportant: Boolean = false,
    val hideRecepients: Boolean = true
)

enum class NotificationType {
    SMS, EMAIL, PUSH_NOTIFICATION, CALL
}