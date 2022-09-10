package com.calendy.resources

import com.calendy.models.*
import com.calendy.services.UserService
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class UserResource {

    @Autowired
    lateinit var userService: UserService

    /**
     * Possibilities: Can create a text based search mechanism with Solr/ElastiSeacrh
     * to search a user based on parameters such as name, email,etc.
     */
    @PostMapping("/createUser")
    fun createUser(@RequestBody @NotNull userRequest: CreateUserRequest): String? {
        return userService.createUser(userRequest)
    }

    @GetMapping("/getAllUsers")
    fun getAllUsers(): List<User> {
        return userService.getAllUsers()
    }

    @GetMapping("/getActiveEventsForUser")
    fun getActiveEventsForUser(@RequestParam @NotNull userId: String): List<CalenderEvent> {
        return userService.getAllActiveEventsForUser(userId)
    }


    /**
     * Primary Requirement:
     */
    @GetMapping("/getUserAvailability")
    fun getUserAvailability(
        @RequestParam @NotNull userId: String,
        @RequestParam @NotNull startofDay: Long,
        @RequestParam @NotNull eventId: String
    ): UserAvailabilityResponse? {
        return userService.getUserAvailability(
            userId, Date(startofDay), eventId
        )
    }

    /**
     * Primary Requirement:
     * Create a Calendly Event:
     * Host can create an event/slots where other users can book slots
     * Host can define the nature  of the following
     * slot : duration, booking window (rolling period or fixed window), workflow(payment),
     * sync with third party calendars (Google Calendar, Outlook etc.)
     * return event id
     */
    @PostMapping("/createEvent")
    fun createEvent(@RequestBody @NotNull request: CalenderEventRequest): String {
        return userService.createUserEvent(request)
    }

    @PostMapping("requestSlotBooking")
    fun requestSlotBooking(@RequestBody request: SlotBookingRequest): String? {
        return userService.bookSlot(request)
    }


    /** TODO: Implement
     * Update a created event :
     * Set availability
     * Redefine Slot Windows
     */
    @PostMapping("/updateEvent")
    fun updateEvent() {

    }

    @PostMapping("/deleteEvent")
    fun deleteEvent() {

    }


}