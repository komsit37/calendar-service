package com.folio.calendar

import java.time.LocalDate

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.{Holiday, HolidayRepo}
import com.folio.calendar.module.QuillDbContextModule
import com.folio.calendar.service.HolidayService
import com.twitter.inject.app.TestInjector
import com.twitter.util.{Await, Future}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class HolidayRepoTest extends WordSpec with Matchers with BeforeAndAfterEach{

val injector = TestInjector(QuillDbContextModule)
  val repo = injector.instance[HolidayRepo]

  override def beforeEach(): Unit = {
    repo.deleteAll.value
  }

  "insert and select" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2016, 2, 14), None)).value
    repo.insert(Holiday(Calendar.Nasdaq, LocalDate.of(2016, 2, 15), None)).value
    val res = repo.select(Calendar.Jpx, LocalDate.of(2016, 1, 1)).value
    info(res.toString)
    res should have length 1
    res.head.calendar shouldBe Calendar.Jpx
    res.head.date shouldBe LocalDate.of(2016,2,14)
    res.head.note shouldBe None
  }
  "insert and select from" in {}

  "insert and select filter calendar" in {}

  "insert and delete" in {}

  "insert and delete one same calendar" in {}

  "insert and delete one different calendar" in {}
  //add more repo tests here

  //to add .value method to future
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }
}
