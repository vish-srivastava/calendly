package com.calendy.services.ThirdPartyCalenders

import com.calendy.models.*
import com.calendy.services.NotificationEngine
import com.calendy.services.ThirdPartyCalenders.implementation.AppleCalenderService
import com.calendy.services.ThirdPartyCalenders.implementation.GoogleCalenderService
import com.calendy.services.ThirdPartyCalenders.implementation.MicrosoftOutlookCalenderService
import com.calendy.services.ThirdPartyCalenders.implementation.ZoomCalenderService
import com.calendy.services.adapters.ThirdPartyCalendarAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ThirdPartyCalendarManager {

    @Autowired
    lateinit var thirdPartyCalendarAdapter: ThirdPartyCalendarAdapter

    @Autowired
    lateinit var notificationEngine: NotificationEngine
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

    fun linkCalendlyAccountWithThirdPartyCalendar(
        thirdPartyCalenderType: ThirdPartyCalenderType,
        user: User
    ): Boolean {
        val thirdPartyCalenderService = getThirdPartyCalender(thirdPartyCalenderType)
        return thirdPartyCalenderService?.linkAccount(thirdPartyCalenderType, user) ?: false
    }

    fun syncEvent(calenderEvent: CalenderEvent, calenderType: ThirdPartyCalenderType) {
        val sync = getThirdPartyCalender(calenderType)?.syncEvent(calenderEvent)
    }

    /**
     * Create a meeting based on calendar type
     */
    fun createMeeting(
        slotBookingRequest: SlotBookingRequest, event: CalenderEvent,
        host: User, guest: User
    ) {
        getCalendarTypeFromEmail(host)?.let {
            val thirdPartyCalenderService = getThirdPartyCalender(it)
            thirdPartyCalenderService?.createMeeting(
                host = host,
                guest = guest,
                calenderEvent = event,
                slotBookingRequest = slotBookingRequest
            )

            notificationEngine.sendEmail(
                guest, Message(
                    messageBody = "Your meeitng has been set with ${host.name} ...full message",
                    messageHeader = "Booking Appointment ID ..."
                )
            )

            notificationEngine.sendEmail(
                host, Message(
                    messageBody = "You have an upcoming booking with ${guest.name} ...full message",
                    messageHeader = "Booking Appointment ID ..."
                )
            )
        }

    }

    fun getCalendarTypeFromEmail(user: User): ThirdPartyCalenderType? {
        if (user.email.contains("@gmail.com")) {
            return ThirdPartyCalenderType.GOOGLE_CALENDAR
        }

        if (user.email.contains("@outlook.com")) {
            return ThirdPartyCalenderType.MICROSOFT_OUTLOOK
        }
        if (user.email.contains("@apple.com")) {
            return ThirdPartyCalenderType.APPLE_CALENDAR
        }
        if (user.email.contains("@zoom.com")) {
            return ThirdPartyCalenderType.ZOOM_CALENDAR
        }

        return null
    }


}