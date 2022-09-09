package com.calendy.services.ThirdPartyCalenders.implementation

import com.calendy.models.CalenderEvent
import com.calendy.models.ThirdPartyCalenderType
import com.calendy.models.User
import com.calendy.services.ThirdPartyCalenders.ThirdPartyCalendarInterface

class AppleCalenderService : ThirdPartyCalendarInterface {

    override fun linkAccount(thirdPartyCalenderType: ThirdPartyCalenderType, user: User): Boolean {
        return true;
    }

    /**
     * Two way sync
     */
    override fun syncEvent(calenderEvent: CalenderEvent): Boolean {
        return true;
    }

    override fun getCalender(user: User) {

    }

}