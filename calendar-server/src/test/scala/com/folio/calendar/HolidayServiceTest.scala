package com.folio.calendar

import java.time.LocalDate

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.{Holiday, HolidayRepo}
import com.folio.calendar.module.QuillDbContextModule
import com.folio.calendar.service.HolidayService
import com.twitter.inject.app.TestInjector
import com.twitter.util.{Await, Future}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class HolidayServiceTest extends WordSpec with Matchers with BeforeAndAfterEach {

  val injector = TestInjector(QuillDbContextModule)
  val service = injector.instance[HolidayService]

  override def beforeEach(): Unit = {
    injector.instance[HolidayRepo].deleteAll.value
  }

  val Mon = LocalDate.of(2017, 2, 13)
  val Tue = Mon.plusDays(1)
  val Wed = Mon.plusDays(2)
  val Thu = Mon.plusDays(3)
  val Fri = Mon.plusDays(4)
  val Sat = Mon.plusDays(5)
  val Sun = Mon.plusDays(6)
  val NextMon = Mon.plusDays(7)
  val NextTue = Mon.plusDays(8)

  "insertHoliday" should {
    "persist holiday" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon, Some("note"))).value
      val res = service.getHolidays(Calendar.Jpx).value
      res shouldBe Seq(Holiday(Calendar.Jpx, Mon, Some("note")))
    }
    "not insert duplicate holiday and return false" in {
      val d = Holiday(Calendar.Jpx, Mon)
      service.insertHoliday(d).value shouldBe true
      service.insertHoliday(d).value shouldBe false
      val res = service.getHolidays(Calendar.Jpx).value
      res shouldBe Seq(d)
    }
  }

  "getHolidays" should {
    "return all holidays" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
      val res = service.getHolidays(Calendar.Jpx).value
      res should contain only Holiday(Calendar.Jpx, Mon)
    }
    "filter by from/to date" should {
      "return holidays after from date" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Wed)).value
        val res = service.getHolidays(Calendar.Jpx, Some(Tue)).value
        res should contain only Holiday(Calendar.Jpx, Wed)
      }
      "from date should be inclusive" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        val res = service.getHolidays(Calendar.Jpx, Some(Mon)).value
        res should contain only Holiday(Calendar.Jpx, Mon)
      }

      "return holidays before to date" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Wed)).value
        val res = service.getHolidays(Calendar.Jpx, Some(Mon), Some(Tue)).value
        res should contain only (Holiday(Calendar.Jpx, Mon), Holiday(Calendar.Jpx, Tue))
      }
      "to date should be inclusive" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
        val res = service.getHolidays(Calendar.Jpx, Some(Mon), Some(Tue)).value
        res should contain only (Holiday(Calendar.Jpx, Mon), Holiday(Calendar.Jpx, Tue))
      }
      "return holidays between from/to date" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Wed)).value
        service.insertHoliday(Holiday(Calendar.Jpx, NextMon)).value
        service.insertHoliday(Holiday(Calendar.Jpx, NextTue)).value

        val res = service.getHolidays(Calendar.Jpx, Some(Tue), Some(NextMon)).value
        res should contain only (Holiday(Calendar.Jpx, Tue), Holiday(Calendar.Jpx, Wed),Holiday(Calendar.Jpx, NextMon))
      }
    }
    "return only holidays for Jpx calendar" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
      service.insertHoliday(Holiday(Calendar.Jpx, Sun)).value
      service.insertHoliday(Holiday(Calendar.Japannext, Thu)).value
      service.insertHoliday(Holiday(Calendar.Nasdaq, Fri)).value
      val res = service.getHolidays(Calendar.Jpx, None).value

      res should contain only(Holiday(Calendar.Jpx, Tue), Holiday(Calendar.Jpx, Sun))
    }
  }

  "deleteAllHolidays" should {
    "delete all holidays" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
      service.getHolidays(Calendar.Jpx).value should have length 1
      service.deleteAllHolidays.value shouldBe true
      service.getHolidays(Calendar.Jpx).value should have length 0
    }
  }

  "deleteHoliday" should {
    "return true" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
      service.getHolidays(Calendar.Jpx).value should have length 1
      service.deleteHoliday(Calendar.Jpx, Mon).value shouldBe true
      service.getHolidays(Calendar.Jpx).value should have length 0
    }
    "return false" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
      service.getHolidays(Calendar.Jpx).value should have length 1
      service.deleteHoliday(Calendar.Jpx, Tue).value shouldBe false
      service.getHolidays(Calendar.Jpx).value should have length 1
    }
    "not affect other calendar" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
      service.getHolidays(Calendar.Jpx).value should have length 1
      service.deleteHoliday(Calendar.Nasdaq, Tue).value shouldBe false
      service.getHolidays(Calendar.Jpx).value should have length 1
    }
  }

  "isWeekend" should {
    "return false for Monday" in {service.isWeekend(Mon) shouldBe false}
    "return true for Sat" in {service.isWeekend(Sat) shouldBe true}
    "return true for Sun" in {service.isWeekend(Sun) shouldBe true}
  }

  "isBusinessDay" should {
    "return true for Monday" in {service.isBusinessDay(Calendar.Jpx, Mon).value shouldBe true}
    "return false if Monday is holiday" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
      service.isBusinessDay(Calendar.Jpx, Mon).value shouldBe false
    }
    "return false for Sat" in {service.isBusinessDay(Calendar.Jpx, Sat).value shouldBe false}
    "return false if Sat is holiday" in {
      service.insertHoliday(Holiday(Calendar.Jpx, Sat)).value
      service.isBusinessDay(Calendar.Jpx, Sat).value shouldBe false
    }
    "return false for Sun" in {service.isBusinessDay(Calendar.Jpx, Sun).value shouldBe false}
  }

  "getNextBusinessDay" when {
    "weekday" should {
      "return same day" in {
        service.getNextBusinessDay(Calendar.Jpx, Mon).value shouldBe Mon
      }
    }
    "holiday" should {
      "return next day" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        service.getNextBusinessDay(Calendar.Jpx, Mon).value shouldBe Tue
      }
    }
    "consecutive holidays" should {
      "return next day" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Mon)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
        service.getNextBusinessDay(Calendar.Jpx, Mon).value shouldBe Wed
        service.getNextBusinessDay(Calendar.Jpx, Tue).value shouldBe Wed
      }
    }
    "weekend" should {
      "return next Monday" in {
        service.getNextBusinessDay(Calendar.Jpx, Sat).value shouldBe NextMon
        service.getNextBusinessDay(Calendar.Jpx, Sun).value shouldBe NextMon
      }
    }
    "holiday before weekend" should {
      "return next Monday" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Fri)).value
        service.getNextBusinessDay(Calendar.Jpx, Fri).value shouldBe NextMon
        service.getNextBusinessDay(Calendar.Jpx, Sat).value shouldBe NextMon
        service.getNextBusinessDay(Calendar.Jpx, Sun).value shouldBe NextMon
      }
    }
    "holiday after weekend" should {
      "return next Tuesday" in {
        service.insertHoliday(Holiday(Calendar.Jpx, NextMon)).value
        service.getNextBusinessDay(Calendar.Jpx, Sat).value shouldBe NextTue
        service.getNextBusinessDay(Calendar.Jpx, Sun).value shouldBe NextTue
        service.getNextBusinessDay(Calendar.Jpx, NextMon).value shouldBe NextTue
      }
    }
    "holiday before and after weekend" should {
      "return next Tuesday" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Fri)).value
        service.insertHoliday(Holiday(Calendar.Jpx, NextMon)).value
        service.getNextBusinessDay(Calendar.Jpx, Fri).value shouldBe NextTue
        service.getNextBusinessDay(Calendar.Jpx, Sat).value shouldBe NextTue
        service.getNextBusinessDay(Calendar.Jpx, Sun).value shouldBe NextTue
        service.getNextBusinessDay(Calendar.Jpx, NextMon).value shouldBe NextTue
      }
    }
  }

  "getPreviousBusinessDay" when {
    "weekday" should {
      "return same day" in {
        service.getPreviousBusinessDay(Calendar.Jpx, Mon).value shouldBe Mon
      }
    }
    "holiday" should {
      "return previous day" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
        service.getPreviousBusinessDay(Calendar.Jpx, Mon).value shouldBe Mon
      }
    }
    "consecutive holidays" should {
      "return previous day" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Tue)).value
        service.insertHoliday(Holiday(Calendar.Jpx, Wed)).value
        service.getPreviousBusinessDay(Calendar.Jpx, Tue).value shouldBe Mon
        service.getPreviousBusinessDay(Calendar.Jpx, Wed).value shouldBe Mon
      }
    }
    "weekend" should {
      "return previous Friday" in {
        service.getPreviousBusinessDay(Calendar.Jpx, Sat).value shouldBe Fri
        service.getPreviousBusinessDay(Calendar.Jpx, Sun).value shouldBe Fri
      }
    }
    "holiday before weekend" should {
      "return previous Thursday" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Fri)).value
        service.getPreviousBusinessDay(Calendar.Jpx, Fri).value shouldBe Thu
        service.getPreviousBusinessDay(Calendar.Jpx, Sat).value shouldBe Thu
        service.getPreviousBusinessDay(Calendar.Jpx, Sun).value shouldBe Thu
      }
    }
    "holiday after weekend" should {
      "return previous Friday" in {
        service.insertHoliday(Holiday(Calendar.Jpx, NextMon)).value
        service.getPreviousBusinessDay(Calendar.Jpx, NextMon).value shouldBe Fri
        service.getPreviousBusinessDay(Calendar.Jpx, Sun).value shouldBe Fri
        service.getPreviousBusinessDay(Calendar.Jpx, Sat).value shouldBe Fri
      }
    }
    "holidays before and after weekend" should {
      "return next Tuesday" in {
        service.insertHoliday(Holiday(Calendar.Jpx, Fri)).value
        service.insertHoliday(Holiday(Calendar.Jpx, NextMon)).value
        service.getPreviousBusinessDay(Calendar.Jpx, NextMon).value shouldBe Thu
        service.getPreviousBusinessDay(Calendar.Jpx, Sun).value shouldBe Thu
        service.getPreviousBusinessDay(Calendar.Jpx, Sat).value shouldBe Thu
        service.getPreviousBusinessDay(Calendar.Jpx, Fri).value shouldBe Thu
      }
    }
  }

  //convenient implicit to add .value to Future type instead of calling Await.result
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }

}
