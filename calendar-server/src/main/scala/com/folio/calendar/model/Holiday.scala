package com.folio.calendar.model

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import com.folio.calendar.idl.Calendar
import io.getquill.{FinagleMysqlContext, Literal}

case class Holiday(calendar: Calendar, date: LocalDate, note: Option[String] = None)

//direct db query
@Singleton
class HolidayRepo @Inject()(val ctx: FinagleMysqlContext[Literal]){
  import ctx._

  implicit val encodeLocalDate = MappedEncoding[LocalDate, String](_.toString())
  implicit val decodeLocalDate = MappedEncoding[String, LocalDate](LocalDate.parse(_))

  implicit val encodeCalendar = MappedEncoding[Calendar, Int](_.getValue)
  implicit val decodeCalendar = MappedEncoding[Int, Calendar](Calendar(_))

  implicit class ForLocalDate(ldt: LocalDate) {
    def > = quote((arg: LocalDate) => infix"$ldt > $arg".as[Boolean])
    def >= = quote((arg: LocalDate) => infix"$ldt >= $arg".as[Boolean])
    def < = quote((arg: LocalDate) => infix"$ldt < $arg".as[Boolean])
    def <= = quote((arg: LocalDate) => infix"$ldt <= $arg".as[Boolean])
    def == = quote((arg: LocalDate) => infix"$ldt = $arg".as[Boolean])
  }

  def selectOne(calendar: Calendar, date: LocalDate) = ctx.run(quote{
    query[Holiday].filter(x => x.calendar == lift(calendar) && x.date == lift(date)).take(1)
  })

  def select(calendar: Calendar, from: LocalDate) = ctx.run(quote{
    query[Holiday].filter(x => x.calendar == lift(calendar) && x.date > lift(from))
  })

  def insert(holiday: Holiday) = ctx.run(quote{
    query[Holiday].insert(lift(holiday))
  })

  def deleteAll = ctx.run(quote{
    query[Holiday].delete
  })

  def delete(calendar: Calendar, date: LocalDate) = ctx.run(quote{
    query[Holiday].filter(x => x.calendar == lift(calendar) && x.date == lift(date)).delete
  })
}