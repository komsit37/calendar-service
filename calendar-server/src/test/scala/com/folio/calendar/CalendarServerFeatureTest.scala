package com.folio.calendar

import java.time.LocalDate

import com.folio.calendar.idl.{Calendar, CalendarService}
import com.folio.calendar.model.HolidayRepo
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.thrift.ThriftClient
import com.twitter.inject.server.FeatureTest
import com.twitter.util.Future
import org.scalatest.BeforeAndAfterEach

class CalendarServerFeatureTest extends FeatureTest with BeforeAndAfterEach {
  override val server = new EmbeddedHttpServer(
    new CalendarServer,
    disableTestLogging = true
  ) with ThriftClient

  val repo = injector.instance[HolidayRepo]
  val client = server.thriftClient[CalendarService[Future]]()

  override def beforeEach(): Unit = {
    repo.deleteAll.value
  }

  val Fri = "2017-02-17"
  val Sat = "2017-02-18"
  val Sun = "2017-02-19"
  val NextMon = "2017-02-20"
  val NextTue = "2017-02-21"

  "thrift" should {
    "getHolidays" in {
      val d = idl.Holiday(Calendar.Jpx, Fri, Some("note"))
      client.insertHoliday(d).value
      val res = client.getHolidays(Calendar.Jpx).value
      res should contain(d)
    }
    "getNextBusinessDay" in {
      client.insertHoliday(idl.Holiday(Calendar.Jpx, NextMon)).value
      client.getNextBusinessDay(Calendar.Jpx, Sat).value shouldBe NextTue
      client.getNextBusinessDay(Calendar.Jpx, Sun).value shouldBe NextTue
      client.getNextBusinessDay(Calendar.Jpx, NextMon).value shouldBe NextTue
    }
    "getPreviousBusinessDay" in {
      client.insertHoliday(idl.Holiday(Calendar.Jpx, NextMon)).value
      client.getPreviousBusinessDay(Calendar.Jpx, NextMon).value shouldBe Fri
      client.getPreviousBusinessDay(Calendar.Jpx, Sun).value shouldBe Fri
      client.getPreviousBusinessDay(Calendar.Jpx, Sat).value shouldBe Fri
    }
  }

  "http" should {
    "get holidays" in {
      server.httpGet(
        path = "/holidays",
        andExpect = Status.Ok
      )
    }
  }
}
