package com.calendy.services

import com.calendy.models.CalenderEvent
import com.calendy.models.ThirdPartyCalenderType
import com.calendy.models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ThirdPartyCalendarService {

    @Autowired
    lateinit var thirdPartCalenderAdapter: ThirdPartCalenderAdapter

    fun linkAccount(thirdPartyCalenderType: ThirdPartyCalenderType, user: User): Boolean {
        // do nothing
        return true
    }

    fun syncEvent(calenderEvent: CalenderEvent): Boolean {
        return true
    }
}