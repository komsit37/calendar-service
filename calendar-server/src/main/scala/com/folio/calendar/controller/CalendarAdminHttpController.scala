package com.folio.calendar.controller

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException
import javax.inject.Inject

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.Holiday
import com.folio.calendar.service.HolidayService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.QueryParam
import com.twitter.finatra.response.Mustache
import com.twitter.util.{Await, Future}

@Mustache("holidays")
case class HolidayListView(
                            holidays: Seq[Holiday],
                            errorMsg: String,
                            calendarId: Int,
                            calendars: List[Calendar]
                          )
case class HolidayListRequest(
                           @QueryParam calendarId: Option[Int]
                         )

case class HolidayRequest(
                           @QueryParam calendar: Int,
                           @QueryParam date: String,
                           @QueryParam note: Some[String],
                           @QueryParam t: String
                         )

class CalendarAdminHttpController @Inject()(
                                             holidayService: HolidayService
                                           ) extends Controller {

  def parseDate(ldStr: String): LocalDate = LocalDate.parse(ldStr, DateTimeFormatter.ISO_LOCAL_DATE)

  get("/holidays") { request: HolidayListRequest =>
    // If the calendarId is not set, set the value to the first one
    val defaulCalendarId = Calendar.list.head.value
    val calendar = Calendar.apply(request.calendarId.getOrElse(defaulCalendarId))
    holidayService.getHolidays(calendar).map(holidays => HolidayListView(holidays, "", request.calendarId.getOrElse(defaulCalendarId), Calendar.list))

  }
  post("/holiday") { request: HolidayRequest =>
    val calendar = Calendar.apply(request.calendar)
    if(request.t == "insert"){
      try {
        val holiday = new Holiday(calendar, parseDate(request.date), request.note)
        holidayService.insertHoliday(holiday).flatMap(result => holidayService.getHolidays(calendar).map(holidays => HolidayListView(holidays, "", request.calendar, Calendar.list)))
      } catch {
        case e: NoSuchElementException => HolidayListView(Seq(), "Cannot find this calendar id",request.calendar, Calendar.list)
        case ex: Exception => HolidayListView(Seq(), "Unknown Error " + ex.getMessage,request.calendar, Calendar.list)
      }
    }else if(request.t == "delete"){
//      holidayService.getHolidays(calendar).map(holidays => HolidayListView(holidays, ""))
      holidayService.deleteHoliday(calendar, parseDate(request.date)).flatMap(_ => holidayService.getHolidays(calendar).map(holidays => HolidayListView(holidays, "", request.calendar, Calendar.list)))
    }


  }

  get("/:*") { request: Request =>
    response.ok.fileOrIndex(
      filePath = request.params("*"),
      indexPath = "index.html")
  }

}
