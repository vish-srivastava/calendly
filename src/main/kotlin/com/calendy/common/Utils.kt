package com.calendy.common

import com.calendy.models.CalenderEvent
import com.calendy.models.CalenderEventRequest
import com.calendy.models.CreateUserRequest
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
        eventDetails = eventDetails,
        slotWindowType = slotWindowType,
        slotMaxDurationMinutes = slotDurationMinutes,
        dailyStartTimeMins = dailyStartTimeMins,
        dailyEndTimeMins = dailyEndTimeMins,
        eventStartDate = eventStartDate,
        eventEndDate = eventEndDate
    )
}