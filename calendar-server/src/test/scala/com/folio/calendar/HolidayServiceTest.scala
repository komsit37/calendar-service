package com.folio.calendar

import java.time.LocalDate

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.{Holiday, HolidayRepo}
import com.folio.calendar.module.QuillDbContextModule
import com.folio.calendar.service.HolidayService
import com.twitter.inject.app.TestInjector
import com.twitter.util.{Await, Future}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class HolidayServiceTest extends WordSpec with Matchers with BeforeAndAfterEach{

val injector = TestInjector(QuillDbContextModule)
  val repo = injector.instance[HolidayRepo]
  val service = injector.instance[HolidayService]

  override def beforeEach(): Unit = {
    repo.deleteAll.value
  }

  "insert and select" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    val res = service.getHolidays(Calendar.Jpx).value
    info(res.toString)
    res should have length 1
    res.head.calendar shouldBe Calendar.Jpx
    res.head.date shouldBe LocalDate.of(2017,2,14)
    res.head.note shouldBe None
  }

  "insert and select from" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 16), None)).value
    val res = service.getHolidays(Calendar.Jpx, Some(LocalDate.of(2017, 2, 15)) ).value
    info(res.toString)
    res should have length 1
    res.head.calendar shouldBe Calendar.Jpx
    res.head.date shouldBe LocalDate.of(2017,2,16)
    res.head.note shouldBe None
  }

  "insert and delete" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    service.deleteAllHolidays
    val res = service.getHolidays(Calendar.Jpx).value
    info(res.toString)
    res should have length 0
  }
  //convenient implicit to add .value to Future type instead of calling Await.result
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }
}
