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
    val holidays = holidayService.getHolidays(Calendar.Jpx).value

    HolidayListView(holidays)
  }

  get("/:*") { request: Request =>
    response.ok.fileOrIndex(
      filePath = request.params("*"),
      indexPath = "index.html")
  }

  //convenient implicit to add .value to Future type instead of calling Await.result
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }
}
