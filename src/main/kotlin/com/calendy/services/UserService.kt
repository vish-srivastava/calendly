package com.calendy.services


import com.calendy.common.*
import com.calendy.daos.EventsRepository
import com.calendy.daos.SlotsRepository
import com.calendy.daos.UserRepository
import com.calendy.models.*
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
    fun getAllUsers(): List<User> {
        return userRepository.findAll();
    }

    fun getAllEventsForUser(userId: String): List<CalenderEvent> {
        return eventsRepository.findAllById(listOf(userId)).filter { it.isActive == true }
    }

    fun createUserEvent(calenderEventRequest: CalenderEventRequest): String {
        try {
            calenderEventRequest.validate()
            val event = eventsRepository.save(calenderEventRequest.toCalenderEvent())
            return event.eventId
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
        user?.let {
            eventsRepository.findByIdOrNull(eventId)?.let { calenderEvent ->

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
                    if (startOfDay.after(calenderEvent.eventEndDate)) {
                        return UserAvailabilityResponse(
                            userId = userId,
                            eventId = eventId,
                            startDate = calenderEvent.eventStartDate,
                            endDate = calenderEvent.eventEndDate,
                            totalAvailableSlots = 0,
                            errorMessage = "Event has already Ended on ${calenderEvent.eventEndDate}"
                        )
                    }
                    if (startOfDay.before(calenderEvent.eventStartDate)) {
                        return UserAvailabilityResponse(
                            userId = userId,
                            eventId = eventId,
                            startDate = calenderEvent.eventStartDate,
                            endDate = calenderEvent.eventEndDate,
                            totalAvailableSlots = 0,
                            errorMessage = "Event has not started, start date : ${calenderEvent.eventStartDate}"
                        )
                    }
                }

                val bookedSlots: List<Slot> = slotRepository.findAllById(listOf(eventId))
                val filteredSlots: List<Slot> =
                    bookedSlots.filter { it.startTime.time >= startOfDay.time && it.endTime.time <= (startOfDay.time + 24 * 60L) }
                        .sortedBy {
                            it.startTime
                        }
                var index = 0
                if (filteredSlots.isNotEmpty()) {
                    val availibiltitMap = mutableMapOf<String, String>()
                    val unavailibiltitMap = mutableMapOf<String, String>()

                    val startTime = startOfDay.time + (calenderEvent.dailyStartTimeMins * 60 * 1000L)
                    val endTime = startOfDay.time + (calenderEvent.dailyEndTimeMins * 60 * 1000L)

                    for (slotTime in startTime..endTime step calenderEvent.slotMaxDurationMinutes * 60 * 1000L) {
                        if (index < filteredSlots.size
                            && filteredSlots.get(index).startTime.time >= slotTime
                            && filteredSlots.get(index).startTime.time <= (slotTime + calenderEvent.slotMaxDurationMinutes * 60 * 1000L)
                        ) {
                            val startMinutes = slotTime / (60 * 1000L) % (24 * 60)
                            val endMinutes = startMinutes.toInt() + calenderEvent.slotMaxDurationMinutes
                            unavailibiltitMap[startMinutes.toInt().toMinutesString()] = endMinutes.toString()
                        } else {
                            val startMinutes = slotTime / (60 * 1000L) % (24 * 60)
                            val endMinutes = startMinutes.toInt() + calenderEvent.slotMaxDurationMinutes
                            availibiltitMap[startMinutes.toString()] = endMinutes.toString()
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
                    for (time in calenderEvent.dailyStartTimeMins..calenderEvent.dailyEndTimeMins step calenderEvent.slotMaxDurationMinutes) {
                        val startTime = time.toMinutesString()
                        val endTime = (time + calenderEvent.slotMaxDurationMinutes).toMinutesString()
                        availibiltitMap[startTime] = endTime
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


            } ?: throw Exception("Invalid Event ID")
        } ?: throw Exception("Invalid User ID")

        return null

    }


    /**
     * Validate request
     * get event detail
     */
    fun bookSlot(request: SlotBookingRequest): String? {
        val event = eventsRepository.findByIdOrNull(request.eventId)
        event?.let {
            userRepository.findByIdOrNull(event.hostUserId)?.let { hostUser ->
                userRepository.findByIdOrNull(request.inviteeUserId)?.let { guestUser ->
                    val existingSlots: List<Slot> = slotRepository.findAllById(listOf(request.eventId))
                    if (existingSlots.isNotEmpty()) {
                        val collidingSlots = existingSlots.filter {
                            request.startTime.after(Date(it.startTime.time - 60 * 1000L)) && request.startTime.before(
                                it.endTime
                            )
                        }.toList()

                        if (collidingSlots.isNotEmpty()) {
                            throw Exception("This Slot is not available")
                        } else {

                            /**
                             * To handle Concurrent Requests , acquire a lock on eventId-startTime based key,
                             * return lock acquisition failure if lock acq. failes
                             */

                            val slot = request.toSlot()
                            slotRepository.save(slot)
                            return slot.slotId
                        }
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
}