package com.calendy.models

import java.util.*

data class CreateUserRequest(
    val name: String,
    val emails: List<String>,
    val phone: String,
    val accountsToLink: Map<ThirdPartyCalenderType, String>
)

data class User(
    val userId: String,
    val name: String,
    val emails: List<String>,
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