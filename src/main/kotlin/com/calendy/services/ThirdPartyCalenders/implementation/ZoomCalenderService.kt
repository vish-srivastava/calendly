package com.calendy.services.ThirdPartyCalenders.implementation

import com.calendy.models.CalenderEvent
import com.calendy.models.SlotBookingRequest
import com.calendy.models.ThirdPartyCalenderType
import com.calendy.models.User
import com.calendy.services.ThirdPartyCalenders.ThirdPartyCalendarInterface

class ZoomCalenderService : ThirdPartyCalendarInterface {

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

    override fun createMeeting(
        host: User,
        guest: User,
        slotBookingRequest: SlotBookingRequest,
        calenderEvent: CalenderEvent
    ) {

    }


}