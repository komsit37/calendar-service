package com.folio.calendar.controller

import java.time.LocalDate
import javax.inject.Inject

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.Holiday
import com.folio.calendar.service.HolidayService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache
import com.twitter.util.{Await, Future}

@Mustache("holidays")
case class HolidayListView(
  holidays: Seq[Holiday]
)

class CalendarAdminHttpController @Inject()(
  holidayService: HolidayService
) extends Controller {

  get("/holidays") {request: Request =>
//    holidayService.getHolidays(Calendar.Jpx)
    holidayService.getHolidays(Calendar.Jpx).map(holidays=>HolidayListView(holidays))

  }

  get("/:*") { request: Request =>
    response.ok.fileOrIndex(
      filePath = request.params("*"),
      indexPath = "index.html")
  }

}
