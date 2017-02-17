package com.folio.calendar.controller

import java.time.LocalDate
import javax.inject.Inject

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.Holiday
import com.folio.calendar.service.HolidayService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache


@Mustache("holidays")
case class HolidayListView(
  holidays: Seq[Holiday]
)

@Mustache("holiday")
case class HolidayDetailView(
                            holiday: Holiday
                          )

class CalendarAdminHttpController @Inject()(
  holidayService: HolidayService
) extends Controller {

  get("/holidays") {request: Request =>
//    holidayService.getHolidays(Calendar.Jpx)
    //return dummy data for now
    HolidayListView(Seq(
      Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), Some("valentine's day")),
      Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 15))
    ))
  }

  get("/holiday") {request: Request =>
    //    holidayService.getHolidays(Calendar.Jpx)
    //return dummy data for now
    HolidayDetailView(
      Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), Some("valentine's day"))
    )
  }

  get("/:*") { request: Request =>
    response.ok.fileOrIndex(
      filePath = request.params("*"),
      indexPath = "index.html")
  }
}