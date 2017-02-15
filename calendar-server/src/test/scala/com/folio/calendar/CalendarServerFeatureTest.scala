package com.folio.calendar

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

  "thrift" should {
    "insert and get holiday" in {
      val d = idl.Holiday(Calendar.Jpx, "2017-02-14", None)
      client.insertHoliday(d).value
      val res = client.getHolidays(Calendar.Jpx).value
      res should contain(d)
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
