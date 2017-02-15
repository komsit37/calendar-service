package com.folio.calendar.service

import java.time.LocalDate
import javax.inject.Inject

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.{Holiday, HolidayRepo}
import com.twitter.util.Future

@Inject
class HolidayService @Inject()(holidayRepo: HolidayRepo){
  //should give a sensible default
  val defaultFrom = LocalDate.of(2017, 1, 1)
  val defaultTo = LocalDate.of(2017, 12, 31)
  def getHolidays(calendar: Calendar, from: Option[LocalDate] = None): Future[Seq[Holiday]] = holidayRepo.select(calendar, from.getOrElse(defaultFrom))
  def insertHoliday(holiday: Holiday): Future[Boolean] = holidayRepo.insert(holiday).map(_ => true)
  //add business logic to db query result
}
