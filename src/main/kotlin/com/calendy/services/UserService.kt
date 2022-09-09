package com.calendy.services

import com.calendy.daos.UserRepository
import com.calendy.models.CreateUserRequest
import com.calendy.models.ThirdPartyCalenderType
import com.calendy.models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class UserService {

    @Autowired
    lateinit var userRepository: UserRepository

    fun getAllUsers(): List<User> {
        return userRepository.findAll();
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

    private inline fun CreateUserRequest.validateUserCreationRequest() {
        // some validations based on email
        val existingUsersForSameEmail = listOf("vishal.srivastava@gmail.com")
//        userRepository.findByUserEmails(listOf(this.email))
        if (existingUsersForSameEmail.isNotEmpty()) {
            throw Exception("User email already exists")
        }
    }

    fun getUserAvailability(userId: String, startDate: Date, endDate: Date) {

    }

    fun updateUserAvailability(userId: String) {

    }

    fun syncThirdPartyCalender(userId: String, accountsToLink: Map<ThirdPartyCalenderType, String>) {

    }
}