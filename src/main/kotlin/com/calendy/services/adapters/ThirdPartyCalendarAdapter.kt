package com.calendy.services.adapters

import com.calendy.models.CalenderEvent
import com.calendy.models.ThirdPartyCalenderType
import org.springframework.stereotype.Service

@Service
class ThirdPartyCalendarAdapter {

    fun adpatCalenderEvent(calenderType: ThirdPartyCalenderType, calenderEvent: CalenderEvent) {
        // adapt based on calendar type
    }
}