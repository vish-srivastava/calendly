package com.calendy.common

import com.calendy.models.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*


inline fun CreateUserRequest.validateUserCreationRequest(exisitngUserEmails: Set<String>): Boolean {

    if (exisitngUserEmails.contains(this.email)) {
        return false
    }
    // some validations based on email
//    val existingUsersForSameEmail = listOf("vishal.srivastava@gmail.com")
////        userRepository.findByUserEmails(listOf(this.email))
//    if (existingUsersForSameEmail.isNotEmpty()) {
//        throw Exception("User email already exists")
//    }

    return true
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
        hostUserId = this.hostUserId,
        eventMetadata = mapOf<String, String>(
            "eventLocation" to (eventMetadata?.let { it.eventLocation.name } ?: ""),
            "eventLocationUrl" to (eventMetadata?.let { it.eventLocationUrl } ?: ""),
            "guestEmails" to (eventMetadata?.let { it.guestEmails.toString() } ?: "")
        )

    )
}

inline fun Int.toMinutesString(): String {
    val hours = this / 60
    val minutes = this % 60
    return "$hours hrs $minutes mins"
}

inline fun SlotBookingRequest.validate(event: CalenderEvent) {
    if ((endTime.time - startTime.time) > event.slotMaxDurationMinutes * 60 * 1000) {
        throw Exception("Max booking duration for this event us ${event.slotMaxDurationMinutes} mins.")
    }
}

inline fun Interval.areIntervalsConflicting(interval2: Interval): Boolean {
    if ((this.startTime > interval2.startTime && this.startTime <= interval2.endTime)
        || (interval2.startTime > this.startTime && interval2.startTime <= this.startTime)
        || (interval2.startTime > this.startTime && interval2.startTime <= this.endTime)

    ) {
        return true
    }
    return false
}

inline fun Date.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("Asia/Kolkata"))
}

inline fun Date.getStartOfTheDay(): Date {
    val localDateTime = this.toLocalDateTime()
    val startOfDay = localDateTime.with(LocalTime.MIN);
    return localDateTimeToDate(startOfDay);

}

inline fun Date.localDateTimeToDate(localDateTime: LocalDateTime): Date {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
}