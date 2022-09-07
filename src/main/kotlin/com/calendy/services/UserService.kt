package com.calendy.services

import com.calendy.models.CreateUserRequest
import com.calendy.models.ThirdPartyCalenderType
import org.springframework.stereotype.Service

@Service
class UserService {

    fun createUser(createUserRequest: CreateUserRequest) {

    }

    fun getUserAvailability(userId: String) {

    }

    fun updateUserAvailability(userId: String) {

    }

    fun syncThirdPartyCalender(userId: String, accountsToLink: Map<ThirdPartyCalenderType, String>) {

    }
}