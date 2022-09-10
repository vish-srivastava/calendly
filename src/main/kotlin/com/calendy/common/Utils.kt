package com.calendy.common

import com.calendy.models.*
import java.util.UUID


inline fun CreateUserRequest.validateUserCreationRequest() {
    // some validations based on email
//    val existingUsersForSameEmail = listOf("vishal.srivastava@gmail.com")
////        userRepository.findByUserEmails(listOf(this.email))
//    if (existingUsersForSameEmail.isNotEmpty()) {
//        throw Exception("User email already exists")
//    }
}

inline fun CalenderEventRequest.validate() {
    if (slotDurationMinutes > (24 * 60) ||
        dailyStartTimeMins >= dailyEndTimeMins ||
        (dailyEndTimeMins - dailyStartTimeMins) < slotDurationMinutes ||
        (eventStartDate != null && eventEndDate != null && eventStartDate!!.after(eventEndDate!!))

    ) {
        throw Exception("Invalid Event Creation request")
    }
}

inline fun CalenderEventRequest.toCalenderEvent(): CalenderEvent {
    return CalenderEvent(
        eventId = "EVENT-" + UUID.randomUUID(),
        isActive = isActive,
        paymentRequired = paymentRequired,
        hostUserId = hostUserId,
        slotWindowType = slotWindowType,
        slotMaxDurationMinutes = slotDurationMinutes,
        dailyStartTimeMins = dailyStartTimeMins,
        dailyEndTimeMins = dailyEndTimeMins,
        eventStartDate = eventStartDate,
        eventEndDate = eventEndDate
    )
}

inline fun SlotBookingRequest.toSlot(eventMetadata: EventMetadata? = null): Slot {
    return Slot(
        eventId = eventId,
        slotId = "SLOT-" + UUID.randomUUID(),
        inviteeUserId = inviteeUserId,
        startTime = startTime,
        endTime = endTime,
        eventMetadata = mapOf<String, String>(
            "eventLocation" to (eventMetadata?.let { it.eventLocation.name } ?: ""),
            "eventLocationUrl" to (eventMetadata?.let { it.eventLocationUrl } ?: ""),
            "guestEmails" to (eventMetadata?.let { it.guestEmails.toString() } ?: "")
        )

    )
}

inline fun Int.toMinutesString(): String {
    val totalMinutes = 24 * 60;
    val hours = this / 60
    val minutes = this % 60
    return "$hours hrs $minutes mins"
}

inline fun SlotBookingRequest.validate(event: CalenderEvent) {
    if ((endTime.time - startTime.time) > event.slotMaxDurationMinutes * 60 * 1000) {
        throw Exception("Max booking duration for this event us ${event.slotMaxDurationMinutes} mins.")
    }
}