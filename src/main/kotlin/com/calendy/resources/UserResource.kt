package com.calendy.resources

import com.calendy.models.CalenderEvent
import com.calendy.models.CalenderEventRequest
import com.calendy.models.CreateUserRequest
import com.calendy.models.User
import com.calendy.services.UserService
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.websocket.server.PathParam

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
    fun getAllUSers(): List<User> {
        return userService.getAllUsers()
    }

    @GetMapping("/getActiveEventsForUser")
    fun getActiveEventsForUser(@RequestParam @NotNull userId: String): List<CalenderEvent> {
        return userService.getAllEventsForUser(userId)
    }


    /**
     * Primary Requirement:
     */
    @GetMapping("/getUserAvailability")
    fun getUserAvailability(
        @RequestParam userId: String,
        @RequestParam startDate: Long,
        @RequestParam endDate: Long,
        @RequestParam eventId: String
    ): String {
        return "Hello"
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


    /** 
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