package com.calendy.models

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.springframework.data.cassandra.core.mapping.Indexed
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

@JsonAutoDetect
@Table("user")
data class User(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val linkedThirdPartyCalenders: Map<ThirdPartyCalenderType, String>
)


@JsonAutoDetect
data class UserMetadata(
    val metadata: Map<String, String>
)

@JsonAutoDetect
data class CalenderEventRequest(
    val paymentRequired: Boolean,
    val eventName: String? = null,
    val isActive: Boolean = true,
    val hostUserId: String,
    val eventDetails: EventMetadata,
    val slotWindowType: SlotWindowType,
    val slotDurationMinutes: Int,
    val dailyStartTimeMins: Int,
    val dailyEndTimeMins: Int,
    val eventStartDate: Date? = null,
    val eventEndDate: Date? = null,
    val timeZone: String? = null
)

@JsonAutoDetect
@Table("event")
data class CalenderEvent(
    @PrimaryKey
    val hostUserId: String,
    @Indexed
    val eventId: String,
    val isActive: Boolean,
    val paymentRequired: Boolean,
    val slotWindowType: SlotWindowType,
    val slotMaxDurationMinutes: Int,
    val dailyStartTimeMins: Int,
    val dailyEndTimeMins: Int,
    val eventStartDate: Date?,
    val eventEndDate: Date?,
    val timeZone: String? = null
)

@JsonAutoDetect
data class SlotBookingRequest(
    val eventId: String,
    val inviteeUserId: String,
    val startTime: Date,
    val endTime: Date,
    val eventMetadata: EventMetadata,
    val paymentMetadata: Map<String, String> = mapOf()
)

@JsonAutoDetect
@Table("slot")
data class Slot(
    @PrimaryKey
    val eventId: String,
    @Indexed
    val slotId: String,
    val inviteeUserId: String,
    val startTime: Date,
    val endTime: Date,
    val eventMetadata: EventMetadata
)

@JsonAutoDetect
data class EventMetadata(
    val eventLocation: EventLocation,
    val eventLocationUrl: String,
    val guestEmails: List<String>
)

@JsonAutoDetect
enum class EventLocation {
    ZOOM,
    GOOGLE_MEET,
    MICROSOFT_TEAMS,
    PHONE,
    IN_PERSON
}


@JsonAutoDetect
enum class CalenderEventType {
    CONSULTATION, INTERVIEW, MEETING
}


enum class SlotWindowType {
    ROLLING, FIXED_WINDOW
}

enum class ThirdPartyCalenderType {
    GOOGLE_CALENDAR,
    MICROSOFT_OUTLOOK,
    APPLE_CALENDAR,
    ZOOM_CALENDAR
}

data class Message(
    val messageBody: String,
    val messageHeader: String? = "",
    val isImportant: Boolean = false,
    val hideRecipients: Boolean = true
)

enum class NotificationType {
    SMS, EMAIL, PUSH_NOTIFICATION, CALL
}