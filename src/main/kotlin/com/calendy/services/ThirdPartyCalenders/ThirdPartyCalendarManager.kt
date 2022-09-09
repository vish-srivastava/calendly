package com.calendy.services.ThirdPartyCalenders

import com.calendy.models.CalenderEvent
import com.calendy.models.ThirdPartyCalenderType
import com.calendy.services.ThirdPartyCalenders.implementation.AppleCalenderService
import com.calendy.services.ThirdPartyCalenders.implementation.GoogleCalenderService
import com.calendy.services.ThirdPartyCalenders.implementation.MicrosoftOutlookCalenderService
import com.calendy.services.ThirdPartyCalenders.implementation.ZoomCalenderService
import com.calendy.services.adapters.ThirdPartyCalendarAdapter
import org.springframework.stereotype.Service

@Service
abstract class ThirdPartyCalendarManager {

    lateinit var thirdPartyCalendarAdapter: ThirdPartyCalendarAdapter
    fun getThirdPartyCalender(calenderType: ThirdPartyCalenderType): ThirdPartyCalendarInterface? {
        when (calenderType) {
            ThirdPartyCalenderType.GOOGLE_CALENDAR -> return GoogleCalenderService()
            ThirdPartyCalenderType.MICROSOFT_OUTLOOK -> return MicrosoftOutlookCalenderService()
            ThirdPartyCalenderType.APPLE_CALENDAR -> return AppleCalenderService()
            ThirdPartyCalenderType.ZOOM_CALENDAR -> return ZoomCalenderService()
            else -> {
                throw NotImplementedError("This Calender mechanism is yet to be implemented")
            }
        }
    }

    fun syncEvent(calenderEvent: CalenderEvent, calenderType: ThirdPartyCalenderType) {
        val sync = getThirdPartyCalender(calenderType)?.syncEvent(calenderEvent)
    }


}