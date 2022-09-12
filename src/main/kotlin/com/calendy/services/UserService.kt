package com.calendy.services


import com.calendy.common.*
import com.calendy.daos.EventsRepository
import com.calendy.daos.SlotsRepository
import com.calendy.daos.UserRepository
import com.calendy.models.*
import com.calendy.services.ThirdPartyCalenders.ThirdPartyCalendarManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService {
    companion object {
        const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
        const val IST_OFFSET_MILLIS = 330 * 60 * 1000L
    }

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var eventsRepository: EventsRepository

    @Autowired
    lateinit var slotRepository: SlotsRepository

    @Autowired
    lateinit var thirdPartyCalendarManager: ThirdPartyCalendarManager
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun getAllActiveEventsForUser(userId: String): List<CalenderEvent> {
        return eventsRepository.findAllEventsForHostId(userId).filter { it.isActive }
    }

    fun createUserEvent(calenderEventRequest: CalenderEventRequest): String {
        try {
            calenderEventRequest.validate()
            userRepository.findByIdOrNull(calenderEventRequest.hostUserId)?.let {
                val event = eventsRepository.save(calenderEventRequest.toCalenderEvent())
                return event.eventId
            } ?: throw Exception("No Valid user with id ${calenderEventRequest.hostUserId} found ")

        } catch (e: Exception) {
            throw Exception("Unable to create event because: ${e.message}, $e")
        }
    }

    fun createUser(createUserRequest: CreateUserRequest): String {
        try {
            createUserRequest.validateUserCreationRequest()
            val user = userRepository.save(
                User(
                    userId = "USER" + UUID.randomUUID(),
                    email = createUserRequest.email,
                    linkedThirdPartyCalenders = createUserRequest.accountsToLink,
                    name = createUserRequest.name,
                    phone = createUserRequest.phone,
                )
            )
            return user.userId
        } catch (e: Exception) {
            throw Exception("unable to create user as ${e.message}, $e")
        }
    }

    fun getUserAvailabilities(userId: String, startOfDay: Date, eventId: String?): List<UserAvailabilityResponse> {
        userRepository.findByIdOrNull(userId)?.let {
            val allActiveSlotsAsHost = slotRepository.findSlotsForHostUserId(userId)
            val allActiveSlotsAsGuest = slotRepository.findSlotsForGuestUserId(userId)
            val allActiveSlotsForUser = allActiveSlotsAsHost.union(allActiveSlotsAsGuest).toSet()
            val activeEventsForUser = eventsRepository.findAllEventsForHostId(userId).toMutableList()
            if (!activeEventsForUser.isEmpty()) {
                val possibleConflictingSlotsForUser = allActiveSlotsForUser.filter {
                    it.startTime >= startOfDay && it.endTime <= (Date(startOfDay.time.plus(ONE_DAY_MILLIS)))
                }
                val eventsToCheckAvailability = eventId?.let {
                    activeEventsForUser.filter { it.eventId == eventId }
                } ?: activeEventsForUser

                if (eventsToCheckAvailability.isNotEmpty()) {
                    /**
                     * Optimization : Below functionality can be parallelised using CoRoutines and processed independently
                     */
                    val userAvailabilityResponses: List<UserAvailabilityResponse> =
                        eventsToCheckAvailability.map { calenderEvent ->

                            val errorMessage = if (calenderEvent.slotWindowType == SlotWindowType.FIXED_WINDOW) {
                                if (startOfDay.time > (calenderEvent.eventEndDate!!.time - calenderEvent.slotMaxDurationMinutes * 60 * 1000L)
                                ) {
                                    "Event has already Ended on ${calenderEvent.eventEndDate}"
                                } else if (startOfDay.time < calenderEvent.eventStartDate!!.time) {
                                    "Event has not started, start date : ${calenderEvent.eventStartDate}"
                                } else {
                                    null
                                }
                            } else null

                            if (errorMessage != null) {
                                UserAvailabilityResponse(
                                    userId = userId,
                                    eventId = calenderEvent.eventId,
                                    errorMessage = errorMessage
                                )
                            } else {
                                val startTime = startOfDay.time + (calenderEvent.dailyStartTimeMins * 60 * 1000L)
                                val endTime = startOfDay.time + (calenderEvent.dailyEndTimeMins * 60 * 1000L)


                                val availableSlots = mutableListOf<Interval>()
                                val unavailableSlots = mutableListOf<Interval>()
                                for (slotTime in startTime..(endTime - calenderEvent.slotMaxDurationMinutes) step calenderEvent.slotMaxDurationMinutes * 60 * 1000L) {
                                    val slotInterval = Interval(
                                        startTime = Date(slotTime).toLocalDateTime(),
                                        endTime = Date(slotTime + (calenderEvent.slotMaxDurationMinutes * 60 * 1000L)).toLocalDateTime()
                                    )
                                    if (!isIntervalConflictingWithSlots(
                                            slotInterval,
                                            possibleConflictingSlotsForUser
                                        )
                                    ) {
                                        availableSlots.add(slotInterval)
                                    } else {
                                        unavailableSlots.add(slotInterval)
                                    }
                                }
                                UserAvailabilityResponse(
                                    userId = userId,
                                    eventId = calenderEvent.eventId,
                                    totalAvailableSlots = availableSlots.size,
                                    availabileSlots = availableSlots,
                                    unavailableSlots = unavailableSlots
                                )
                            }
                        }.toList()

                    return userAvailabilityResponses

                } else {
                    return listOf(
                        UserAvailabilityResponse(
                            errorMessage = "User doesn't have any active events",
                            userId = userId,
                            eventId = eventId
                        )
                    )
                }
            } else {
                return listOf(
                    UserAvailabilityResponse(
                        errorMessage = "User doesn't have any active events",
                        userId = userId,
                        eventId = eventId
                    )
                )
            }
        } ?: return listOf(
            UserAvailabilityResponse(
                errorMessage = "User doesn't exist",
                userId = userId,
                eventId = eventId
            )
        )

    }

    /**
     * Ignore:  Unused
     */
    @SuppressWarnings("unused")
    fun getUserAvailability(userId: String, startOfDay: Date, eventId: String?): UserAvailabilityResponse? {
        val user = userRepository.findByIdOrNull(userId)
        val istTimeZoneOffSet = 330
        user?.let {
            getAllActiveEventsForUser(userId).filter { it.eventId == eventId }.firstOrNull()?.let { calenderEvent ->
                if (!calenderEvent.isActive) {
                    return UserAvailabilityResponse(
                        userId = userId,
                        eventId = eventId,
                        startDate = calenderEvent.eventStartDate,
                        endDate = calenderEvent.eventEndDate,
                        totalAvailableSlots = 0,
                        errorMessage = "User is Not Accepting Reservations for this Event"
                    )
                }

                if (calenderEvent.slotWindowType == SlotWindowType.FIXED_WINDOW) {
                    val errorMessage =
                        if (startOfDay.time > (calenderEvent.eventEndDate!!.time - calenderEvent.slotMaxDurationMinutes * 60 * 1000L)
                        ) {
                            "Event has already Ended on ${calenderEvent.eventEndDate}"
                        } else if (startOfDay.time < calenderEvent.eventStartDate!!.time) {
                            "Event has not started, start date : ${calenderEvent.eventStartDate}"
                        } else {
                            null
                        }
                    errorMessage?.let {
                        return UserAvailabilityResponse(
                            userId = userId,
                            eventId = eventId,
                            startDate = calenderEvent.eventStartDate,
                            endDate = calenderEvent.eventEndDate,
                            totalAvailableSlots = 0,
                            errorMessage = errorMessage
                        )
                    }
                }

                val bookedSlots: List<Slot> = slotRepository.findSlotsForEventId(eventId!!)
                val filteredSlots: List<Slot> =
                    bookedSlots.filter { it.startTime.time >= startOfDay.time && it.endTime.time <= (startOfDay.time + calenderEvent.dailyEndTimeMins * 60 * 1000L) }
                        .sortedBy {
                            it.startTime
                        }
                var index = 0
                if (filteredSlots.isNotEmpty()) {

                    val availibiltitMap = mutableMapOf<String, String>()
                    val unavailibiltitMap = mutableMapOf<String, String>()
                    val startTime = startOfDay.time + (calenderEvent.dailyStartTimeMins * 60 * 1000L)
                    val endTime = startOfDay.time + (calenderEvent.dailyEndTimeMins * 60 * 1000L)

                    for (slotTime in startTime..(endTime - calenderEvent.slotMaxDurationMinutes) step calenderEvent.slotMaxDurationMinutes * 60 * 1000L) {
                        if (index < filteredSlots.size
                            && filteredSlots.get(index).startTime.time >= slotTime
                            && filteredSlots.get(index).startTime.time <= (slotTime + calenderEvent.slotMaxDurationMinutes * 60 * 1000L)
                        ) {
                            val startMinutes = slotTime / (60 * 1000L) % (24 * 60)
                            val endMinutes = startMinutes.toInt() + calenderEvent.slotMaxDurationMinutes
                            unavailibiltitMap[startMinutes.plus(istTimeZoneOffSet).toInt().toMinutesString()] =
                                endMinutes.plus(istTimeZoneOffSet).toMinutesString()
                            index++
                        } else {
                            val startMinutes = slotTime / (60 * 1000L) % (24 * 60)
                            val endMinutes = startMinutes.toInt() + calenderEvent.slotMaxDurationMinutes
                            val include =
                                calenderEvent.eventEndDate?.let { (startOfDay.time + (endMinutes * 60 * 1000L)) <= calenderEvent.eventEndDate.time }
                                    ?: true

                            if (include) {
                                availibiltitMap[startMinutes.plus(istTimeZoneOffSet).toInt().toMinutesString()] =
                                    endMinutes.plus(istTimeZoneOffSet).toMinutesString()
                            }
                        }

                    }

                    return UserAvailabilityResponse(
                        userId = userId,
                        eventId = eventId,
                        startDate = calenderEvent.eventStartDate,
                        endDate = calenderEvent.eventEndDate,
                        totalAvailableSlots = availibiltitMap.size,
                        availabileSlots = emptyList(),
                        unavailableSlots = emptyList()
                    )

                } else {
                    val availibiltitMap = mutableMapOf<String, String>()
                    for (time in calenderEvent.dailyStartTimeMins..(calenderEvent.dailyEndTimeMins - calenderEvent.slotMaxDurationMinutes) step calenderEvent.slotMaxDurationMinutes) {
                        val endTime = (time + calenderEvent.slotMaxDurationMinutes)
                        val include =
                            calenderEvent.eventEndDate?.let { (startOfDay.time + (endTime * 60 * 1000L)) <= calenderEvent.eventEndDate.time }
                                ?: true

                        if (include) {
                            availibiltitMap[time.plus(istTimeZoneOffSet).toMinutesString()] =
                                endTime.plus(istTimeZoneOffSet).toMinutesString()
                        } else {
                            break
                        }
                    }
                    return UserAvailabilityResponse(
                        userId = userId,
                        eventId = eventId,
                        startDate = calenderEvent.eventStartDate,
                        endDate = calenderEvent.eventEndDate,
                        totalAvailableSlots = availibiltitMap.size,
                        availabileSlots = emptyList()
                    )
                }


            } ?: return UserAvailabilityResponse(
                userId = userId,
                errorMessage = "User has no active events"
            )
        } ?: throw Exception("Invalid User ID")


    }


    /**
     * Validate request
     * get event detail
     *
     * Logic :
     * 1.Get Active User Events for requested Event
     * 2.Get All Other events - excluding the current events
     * 3.Get All slots for events in #2
     * 4.Check slot collisions between requested slot in #1 na all slots in #3
     * 4.
     */
    fun bookSlot(request: SlotBookingRequest): SlotBookingResponse {

        val slotBookingResponse = SlotBookingResponse(request.hostUserId, request.eventId, request.inviteeUserId)
        val event = eventsRepository.findAllEventsForHostId(request.hostUserId).filter { it.eventId == request.eventId }
            .firstOrNull()
        event?.let {
            request.validate(it)
            userRepository.findByIdOrNull(event.hostUserId)?.let { hostUser ->
                userRepository.findByIdOrNull(request.inviteeUserId)?.let { guestUser ->

                    if (hostUser.userId.equals(guestUser.userId, ignoreCase = true)) {
                        return slotBookingResponse.copy(errorMessage = "Guest and host can't be the same")
                    }

                    val startOfDay =
                        Date(request.startTime.time - request.startTime.time % ONE_DAY_MILLIS - IST_OFFSET_MILLIS)
                    val requestedInterval =
                        Interval(request.startTime.toLocalDateTime(), request.endTime.toLocalDateTime())

                    // get and check host's availability for all of his events
                    val userAvailabilityForHost =
                        getUserAvailabilities(hostUser.userId, startOfDay, null).filter { response ->
                            response.eventId == event.eventId
                        }

                    // get and also check host's availability for all of his events
                    val userAvailabilityForGuest =
                        getUserAvailabilities(guestUser.userId, startOfDay, null).filter { response ->
                            response.eventId == event.eventId
                        }

                    // IF either of their calender events collide with request slot booking time, fail booking

                    if (checkTimeIntervalConflictForUserAvailability(
                            requestedInterval,
                            userAvailabilityForGuest
                        ) || checkTimeIntervalConflictForUserAvailability(
                            requestedInterval, userAvailabilityForHost
                        )
                    ) {

                        return slotBookingResponse.copy(errorMessage = "The requested Slot is unavailable/colliding with existing event")
                    }
                    /**
                     * To handle Concurrent Requests , acquire a lock on eventId-startTime based hash/key,
                     * return lock acquisition failure if lock acq. fails
                     */

                    val eventMetadata = generateEventMetadataForSlotBooking(event)
                    val slot = request.toSlot(eventMetadata)
                    slotRepository.save(slot)
                    /**
                     * Create Sync with third Party Calendars
                     */
                    thirdPartyCalendarManager.createMeeting(
                        slotBookingRequest = request,
                        guest = guestUser,
                        host = hostUser,
                        event = event
                    )
                    return slotBookingResponse.copy(slotId = slot.slotId)

                } ?: return slotBookingResponse.copy(errorMessage = "Guest user does not exist")
            } ?: return slotBookingResponse.copy(errorMessage = "Host user is not active anymore")
        } ?: return slotBookingResponse.copy(errorMessage = "No Event with id ${request.eventId} found")
    }


    fun deleteEvent(userId: String, eventId: String) {
        userRepository.findByIdOrNull(userId)?.let {
            eventsRepository.findByIdOrNull(eventId)?.let { calenderEvent ->
                if (!calenderEvent.hostUserId.equals(userId, ignoreCase = true)) {
                    throw Exception("Request User with id $userId is not authorized for this operation")
                } else {
                    try {
                        eventsRepository.save(calenderEvent.copy(isActive = false))
                    } catch (e: Exception) {
                        throw Exception("Unable to update event entity because ${e.message}, $e")
                    }
                }
            } ?: throw Exception("No Event with id $eventId found")
        } ?: throw Exception("Invalid user")
    }


    // TODO
    fun updateUserAvailability(userId: String) {

    }

    fun isIntervalConflictingWithSlots(interval: Interval, slots: List<Slot>): Boolean {
        slots.map { slot ->
            val slotInterval = Interval(
                startTime = slot.startTime.toLocalDateTime(),
                endTime = slot.endTime.toLocalDateTime()
            )

            if (interval.areIntervalsConflicting(slotInterval)) {
                return true
            }
        }
        return false
    }

    //TODO
    fun syncThirdPartyCalender(userId: String, accountsToLink: Map<ThirdPartyCalenderType, String>) {

    }

    fun checkTimeIntervalConflictForUserAvailability(
        interval: Interval,
        userAvailabilityResponses: List<UserAvailabilityResponse>
    ): Boolean {
        userAvailabilityResponses.map { userAvailability ->
            userAvailability.unavailableSlots.map { unavailableSlot ->
                if (interval.areIntervalsConflicting(unavailableSlot)) {
                    return true
                }
            }
        }

        return false
    }

    fun generateEventMetadataForSlotBooking(event: CalenderEvent): EventMetadata {

        return EventMetadata(
            eventLocation = EventLocation.values().get((System.currentTimeMillis() % 4L).toInt()),
            eventLocationUrl = "+91 - 1234567789",
            guestEmails = emptyList()
        )
    }


}