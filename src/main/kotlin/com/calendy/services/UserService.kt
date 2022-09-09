package com.calendy.services

import com.calendy.common.toCalenderEvent
import com.calendy.common.validate
import com.calendy.common.validateUserCreationRequest
import com.calendy.daos.EventsRepository
import com.calendy.daos.UserRepository
import com.calendy.models.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class UserService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var eventsRepository: EventsRepository
    fun getAllUsers(): List<User> {
        return userRepository.findAll();
    }

    fun getAllEventsForUser(userId: String): List<CalenderEvent> {
        return eventsRepository.findAllById(listOf(userId))?.filter { it.isActive == true }
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


    fun getUserAvailability(userId: String, startDate: Date, endDate: Date) {

    }

    fun updateUserAvailability(userId: String) {

    }

    fun syncThirdPartyCalender(userId: String, accountsToLink: Map<ThirdPartyCalenderType, String>) {

    }
}