package com.calendy.resources

import com.calendy.common.toLocalDateTime
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

    companion object {
        const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }

    @Autowired
    lateinit var userService: UserService

    /**
     * Future Optimizations: Can create a text based search mechanism with Solr/ElastiSeacrh
     * to search a user/event etc for faster/ better searching based on parameters such as name, email,etc.
     */
    @PostMapping("/createUser")
    fun createUser(@RequestBody @NotNull userRequest: CreateUserRequest): CreateUserResponse {
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

    @GetMapping("/getUserAvailability")
    fun getUserAvailability(
        @RequestParam @NotNull userId: String,
        @RequestParam @NotNull startofDay: Long,
        @RequestParam eventId: String? = null
    ): List<UserAvailabilityResponse> {
        return userService.getUserAvailabilities(
            userId, Date(startofDay), eventId
        )
    }

    @PostMapping("/createEvent")
    fun createEvent(@RequestBody @NotNull request: CalenderEventRequest): String {
        return userService.createUserEvent(request)
    }

    @PostMapping("requestSlotBooking")
    fun requestSlotBooking(@RequestBody @NotNull request: SlotBookingRequest): SlotBookingResponse {
        return userService.bookSlot(request)
    }


    @PostMapping("/deleteEvent")
    fun deleteEvent(
        @RequestParam @NotNull userId: String,
        @RequestParam @NotNull eventId: String
    ) {
        userService.deleteEvent(userId, eventId)
    }


}