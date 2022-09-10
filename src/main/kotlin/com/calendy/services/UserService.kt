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

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var eventsRepository: EventsRepository

    @Autowired
    lateinit var slotRepository: SlotsRepository

    @Autowired
    lateinit var thirdPartyCalendarManager: ThirdPartyCalendarManager
    fun getAllUsers(): List<User> {
        return userRepository.findAll();
    }

    fun getAllActiveEventsForUser(userId: String): List<CalenderEvent> {
        return eventsRepository.findAllEventsForHostId(userId).filter { it.isActive == true }
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
            throw Exception("unable to create user as ${e.message}, ${e.toString()}")
        }
    }


    /**
     * Given a fixed Day -> return the availability for that day
     */
    fun getUserAvailability(userId: String, startOfDay: Date, eventId: String): UserAvailabilityResponse? {
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

                val bookedSlots: List<Slot> = slotRepository.findSlotsForEventId(eventId)
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
                                    endMinutes.plus(istTimeZoneOffSet).toInt().toMinutesString()
                            }
                        }

                    }

                    return UserAvailabilityResponse(
                        userId = userId,
                        eventId = eventId,
                        startDate = calenderEvent.eventStartDate,
                        endDate = calenderEvent.eventEndDate,
                        totalAvailableSlots = availibiltitMap.size,
                        availabilityMap = availibiltitMap,
                        unavailabilityMap = unavailibiltitMap
                    )

                } else {
                    val availibiltitMap = mutableMapOf<String, String>()
                    for (time in calenderEvent.dailyStartTimeMins..(calenderEvent.dailyEndTimeMins - calenderEvent.slotMaxDurationMinutes) step calenderEvent.slotMaxDurationMinutes) {
                        val startTime = time
                        val endTime = (time + calenderEvent.slotMaxDurationMinutes)
                        val include =
                            calenderEvent.eventEndDate?.let { (startOfDay.time + (endTime * 60 * 1000L)) <= calenderEvent.eventEndDate.time }
                                ?: true

                        if (include) {
                            availibiltitMap[startTime.plus(istTimeZoneOffSet).toMinutesString()] =
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
                        availabilityMap = availibiltitMap
                    )
                }


            } ?: return UserAvailabilityResponse(
                userId = userId,
                errorMessage = "User has no active events"
            )
        } ?: throw Exception("Invalid User ID")

        return null

    }


    /**
     * Validate request
     * get event detail
     */
    fun bookSlot(request: SlotBookingRequest): String? {
        val event = eventsRepository.findAllEventsForHostId(request.hostUserId).filter { it.eventId == request.eventId }
            .firstOrNull()
        event?.let {
            request.validate(it)
            userRepository.findByIdOrNull(event.hostUserId)?.let { hostUser ->
                userRepository.findByIdOrNull(request.inviteeUserId)?.let { guestUser ->

                    if (hostUser.userId.equals(guestUser.userId, ignoreCase = true)) {
                        throw Exception("Guest and host can't be the same")
                    }

                    val existingSlots: List<Slot> = slotRepository.findSlotsForEventId(request.eventId)

                    val collidingSlots = existingSlots.filter {
                        request.startTime.after(Date(it.startTime.time - 60 * 1000L)) && request.startTime.before(
                            it.endTime
                        )
                    }.toList()

                    if (collidingSlots.isNotEmpty()) {
                        throw Exception("This Slot is not available")
                    } else {

                        /**
                         * To handle Concurrent Requests , acquire a lock on eventId-startTime based hash/key,
                         * return lock acquisition failure if lock acq. failes
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

                        return slot.slotId
                    }

                } ?: throw Exception("Guest user does not exist")
            } ?: throw Exception("Host user is not active anymore")


        } ?: throw Exception("No Event with id ${request.eventId} found")

        return null
    }

    fun deleteEvent(userId: String, eventId: String) {
        userRepository.findByIdOrNull(userId)?.let { user ->
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

    fun updateUserAvailability(userId: String) {

    }

    fun syncThirdPartyCalender(userId: String, accountsToLink: Map<ThirdPartyCalenderType, String>) {

    }

    fun generateEventMetadataForSlotBooking(event: CalenderEvent): EventMetadata {

        return EventMetadata(
            eventLocation = EventLocation.values().get((System.currentTimeMillis() % 2L).toInt()),
            eventLocationUrl = "+91 - 9933384625",
            guestEmails = emptyList()
        )
    }
}