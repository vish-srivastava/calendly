package com.calendy.services.ThirdPartyCalenders

import com.calendy.models.CalenderEvent
import com.calendy.models.ThirdPartyCalenderType
import com.calendy.models.User
import org.springframework.stereotype.Service

@Service
interface ThirdPartyCalendarInterface {
    fun linkAccount(thirdPartyCalenderType: ThirdPartyCalenderType, user: User): Boolean

    /**
     * Two way sync
     */
    fun syncEvent(calenderEvent: CalenderEvent): Boolean

    fun getCalender(user: User)
}