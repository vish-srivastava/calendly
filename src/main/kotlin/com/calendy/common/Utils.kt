package com.calendy.common

import com.calendy.models.*
import java.util.UUID


inline fun CreateUserRequest.validateUserCreationRequest() {
    // some validations based on email
    val existingUsersForSameEmail = listOf("vishal.srivastava@gmail.com")
//        userRepository.findByUserEmails(listOf(this.email))
    if (existingUsersForSameEmail.isNotEmpty()) {
        throw Exception("User email already exists")
    }
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

inline fun SlotBookingRequest.toSlot(): Slot {
    return Slot(
        eventId = eventId,
        slotId = "SLOT-" + UUID.randomUUID(),
        inviteeUserId = inviteeUserId,
        startTime = startTime,
        endTime = endTime,
        eventMetadata = eventMetadata

    )
}

inline fun Int.toMinutesString(): String {
    val totalMinutes = 24 * 60;
    val hours = this / 60
    val minutes = this % 60
    return "$hours hr $minutes"
}