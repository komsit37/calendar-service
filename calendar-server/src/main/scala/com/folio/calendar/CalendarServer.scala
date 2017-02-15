package com.folio.calendar

import com.folio.calendar.controller.{CalendarAdminHttpController, CalendarController}
import com.folio.calendar.filter.ExceptionTranslationFilter
import com.folio.calendar.module.QuillDbContextModule
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.finatra.json.utils.CamelCasePropertyNamingStrategy
import com.twitter.finatra.thrift
import com.twitter.finatra.thrift.ThriftServer
import com.twitter.finatra.thrift.filters.{AccessLoggingFilter, StatsFilter, ThriftMDCFilter}
import com.twitter.finatra.thrift.routing.ThriftRouter

//To refresh static html/template files without restarting the server, add '-local.doc.root=thrift-example-server/src/main/webapp' to Program arguments in Intellij's Run Configuration
object CalendarServerMain extends CalendarServer

object CustomJacksonModule extends FinatraJacksonModule {
  override val propertyNamingStrategy = CamelCasePropertyNamingStrategy
}

class CalendarServer extends HttpServer with ThriftServer {
  override val name = "calculator-server"
  override val defaultFinatraHttpPort: String = ":9910"
  override val defaultFinatraThriftPort: String = ":9911"
  override def defaultHttpPort: Int = 9912 //server admin port
  override def jacksonModule = CustomJacksonModule

  override def modules = Seq(
    QuillDbContextModule) //module for quill

  override def configureThrift(router: ThriftRouter) {
    router
      .filter[thrift.filters.LoggingMDCFilter]
      .filter[thrift.filters.TraceIdMDCFilter]
      .filter[ThriftMDCFilter]
      .filter[AccessLoggingFilter]
      .filter[StatsFilter]
      .filter[ExceptionTranslationFilter]
      .add[CalendarController]
  }

  override protected def configureHttp(router: HttpRouter) = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .filter(new Cors.HttpFilter(Cors.UnsafePermissivePolicy))
      .add[CalendarAdminHttpController]
  }
}
