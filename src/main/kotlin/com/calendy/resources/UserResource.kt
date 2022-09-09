package com.calendy.resources

import com.calendy.models.CreateUserRequest
import com.calendy.models.User
import com.calendy.services.UserService
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserResource {

    @Autowired
    lateinit var userService: UserService

    @GetMapping("/getAllUsers")
    fun getAllUSers(): List<User> {
        return userService.getAllUsers()
    }

    @PostMapping("/createUser")
    fun createUser(@RequestBody @NotNull userRequest: CreateUserRequest): String? {
        return userService.createUser(userRequest)
    }

    @GetMapping("/getUserAvailability")
    fun getUserInfo(): String {
        println("Hello")
        return "Hello"
    }

    /**
     * Create a Calendly Event:
     * Host can create an event/slots where other users can book slots
     * Host can define the nature  of the following
     * slot : duration, booking window (rolling period or fixed window), workflow(payment),
     * sync with third party calendars (Google Calendar, Outlook etc.)
     */
    @PostMapping("/createEvent")
    fun createEvent() {

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