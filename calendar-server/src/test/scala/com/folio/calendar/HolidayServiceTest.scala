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

  "insert and select filter calendar" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 19), None)).value
    repo.insert(Holiday(Calendar.Japannext, LocalDate.of(2017, 2, 16), None)).value
    repo.insert(Holiday(Calendar.Nasdaq, LocalDate.of(2017, 2, 17), None)).value
    val res = service.getHolidays(Calendar.Jpx, None).value
    info(res.toString)
    res should have length 2
    res.head.calendar shouldBe Calendar.Jpx
    res.head.date shouldBe LocalDate.of(2017,2,14)
    res.head.note shouldBe None
    res(1).calendar shouldBe Calendar.Jpx
    res(1).date shouldBe LocalDate.of(2017,2,19)
    res(1).note shouldBe None
  }

  "insert and delete" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    service.deleteAllHolidays.value
    val res = service.getHolidays(Calendar.Jpx).value
    info(res.toString)
    res should have length 0
  }

  "insert and delete one same calendar available date" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value



    service.deleteHoliday(Calendar.Jpx, LocalDate.of(2017, 2, 14))
    val res = service.getHolidays(Calendar.Jpx).value
    info(res.toString)
    res should have length 0
  }

  "insert and delete one same calendar not available date" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value

    service.deleteHoliday(Calendar.Jpx, LocalDate.of(2017, 2, 15))
    val res = service.getHolidays(Calendar.Jpx).value
    info(res.toString)
    res should have length 1
    res.head.date shouldBe LocalDate.of(2017,2,14)
    res.head.note shouldBe None
  }

  "insert and delete one different calendar" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    service.deleteHoliday(Calendar.Nasdaq, LocalDate.of(2017, 2, 14))
    val res = service.getHolidays(Calendar.Jpx).value
    info(res.toString)
    res should have length 1
    res.head.date shouldBe LocalDate.of(2017,2,14)
    res.head.note shouldBe None
  }
  "check is weekend" in {
    service.isWeekend(LocalDate.of(2017,2,13)) shouldBe false
    // Saturday
    service.isWeekend(LocalDate.of(2017,2,18)) shouldBe true
    // Sunday
    service.isWeekend(LocalDate.of(2017,2,19)) shouldBe true
  }
  "insert and check is holiday" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value

    var res = service.isHoliday(Calendar.Jpx, LocalDate.of(2017,2,13)).value
    res shouldBe false
    // Holiday case
    var res2 = service.isHoliday(Calendar.Jpx, LocalDate.of(2017,2,14)).value
    res2 shouldBe true
    // Weekend case
    var res3 = service.isHoliday(Calendar.Jpx, LocalDate.of(2017,2,18)).value
    res3 shouldBe true
  }

  "insert and check is business day" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value

    var res = service.isBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,13)).value
    res shouldBe true
    // Holiday case
    var res2 = service.isBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,14)).value
    res2 shouldBe false
    // Weekend case
    var res3 = service.isBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,18)).value
    res3 shouldBe false
  }

  "insert and get next business day without holiday" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    //
    val res = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,13)).value
    res shouldBe LocalDate.of(2017,2,13)

    val res2 = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,14)).value
    res2 shouldBe LocalDate.of(2017,2,15)

    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 15), None)).value

    val res3 = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,14)).value
    res3 shouldBe LocalDate.of(2017,2,16)


  }

  "insert and get next business day with holiday" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 17), None)).value
    //
    val res = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,16)).value
    res shouldBe LocalDate.of(2017,2,16)

    val res2 = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,17)).value
    res2 shouldBe LocalDate.of(2017,2,20)

    val res3 = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,18)).value
    res3 shouldBe LocalDate.of(2017,2,20)

    val res4 = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,19)).value
    res4 shouldBe LocalDate.of(2017,2,20)

    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 20), None)).value
    val res5 = service.getNextBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,17)).value
    res5 shouldBe LocalDate.of(2017,2,21)
  }

  "insert and get previous business day without holiday" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 14), None)).value
    //
    val res = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,15)).value
    res shouldBe LocalDate.of(2017,2,15)

    val res2 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,14)).value
    res2 shouldBe LocalDate.of(2017,2,13)

    // Handle 2 consucutive holidays
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 15), None)).value

    val res3 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,15)).value
    res3 shouldBe LocalDate.of(2017,2,13)


  }

  "insert and get previous business day with holiday" in {
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 17), None)).value
    //
    val res = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,20)).value
    res shouldBe LocalDate.of(2017,2,20)

    val res2 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,19)).value
    res2 shouldBe LocalDate.of(2017,2,16)

    val res3 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,18)).value
    res3 shouldBe LocalDate.of(2017,2,16)

    val res4 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,17)).value
    res4 shouldBe LocalDate.of(2017,2,16)

    // Handle 2 consucutive holidays
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 16), None)).value
    val res5 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,19)).value
    res5 shouldBe LocalDate.of(2017,2,15)

    // Handle holiday -> weekend -> holiday
    repo.insert(Holiday(Calendar.Jpx, LocalDate.of(2017, 2, 20), None)).value
    val res6 = service.getPreviousBusinessDay(Calendar.Jpx, LocalDate.of(2017,2,20)).value
    res6 shouldBe LocalDate.of(2017,2,15)
  }
  //convenient implicit to add .value to Future type instead of calling Await.result
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }
}
