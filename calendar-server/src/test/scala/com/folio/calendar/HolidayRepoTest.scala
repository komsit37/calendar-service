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
    val res = repo.select(Calendar.Jpx, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)).value
    info(res.toString)
    res should have length 1
    res.head.calendar shouldBe Calendar.Jpx
    res.head.date shouldBe LocalDate.of(2016,2,14)
    res.head.note shouldBe None
  }

  "inserting duplicate Holiday" should {
    "throw Exception" in {
      repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2016, 2, 14), None)).value

      //assert exception here - I think if you add primary key to db, insert will throw some mysql error
      //repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2016, 2, 14), None)).value
    }
  }

  "insert and delete" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2016, 2, 14), None)).value
    repo.insert(Holiday(Calendar.Nasdaq, LocalDate.of(2016, 2, 14), None)).value

    repo.delete(Calendar.Nasdaq, LocalDate.of(2016, 2, 14)).value

    val res = repo.select(Calendar.Jpx, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)).value
    res should have length 1
    res.head.calendar shouldBe Calendar.Jpx
    res.head.date shouldBe LocalDate.of(2016,2,14)
    res.head.note shouldBe None

    val res2 = repo.select(Calendar.Nasdaq, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)).value
    res2 should have length 0
  }

  "insert and select one" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2016, 2, 14), None)).value
    // Select that date that do not have in DB
    val res = repo.selectOne(Calendar.Jpx, LocalDate.of(2016, 1, 1)).value
    res should have length 0
    // Select that date that have in DB
    val res2 = repo.selectOne(Calendar.Jpx, LocalDate.of(2016, 2, 14)).value
    res2 should have length 1
    res2.head.calendar shouldBe Calendar.Jpx
    res2.head.date shouldBe LocalDate.of(2016,2,14)
    res2.head.note shouldBe None
  }

  //to add .value method to future
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }
}
