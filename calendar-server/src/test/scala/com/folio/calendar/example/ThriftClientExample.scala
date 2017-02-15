package com.folio.calendar.example

import com.folio.calendar.idl.{Calendar, CalendarService, Holiday}
import com.twitter.finagle.ThriftMux
import com.twitter.util.{Await, Future}

object ThriftClientExample extends App{
  val remoteServer = "localhost:9911"
  val client = ThriftMux.Client()
    .newIface[CalendarService[Future]](remoteServer, "calendar-server")
  println("Calling insertHoliday and getHolidays on remote thrift server: " + remoteServer + "...")
  Await.result(client.insertHoliday(Holiday(Calendar.Jpx, "2017-02-15")))
  val res = Await.result(client.getHolidays(Calendar.Jpx))
  println("Result is " + res)
}
