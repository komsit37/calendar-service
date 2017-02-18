package com.folio.calendar.service

import java.time.{DayOfWeek, LocalDate, ZoneId}
import javax.inject.Inject

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.{Holiday, HolidayRepo}
import com.twitter.util.{Await, Future}

@Inject
class HolidayService @Inject()(holidayRepo: HolidayRepo) {
  def findNextPrevBusinessDay(calendar: Calendar, date: LocalDate, inc: Int): LocalDate = {
    if (isBusinessDay(calendar, date).value) {
      date
    } else {
      if(inc > 0){
        findNextPrevBusinessDay(calendar, date.plusDays(inc), inc)
      }else{
        findNextPrevBusinessDay(calendar, date.minusDays(inc), inc)
      }

    }
  }

  //should give a sensible default
  val defaultFrom = LocalDate.of(2017, 1, 1)
  val defaultTo = LocalDate.of(2017, 12, 31)

  def getHolidays(calendar: Calendar, from: Option[LocalDate] = None): Future[Seq[Holiday]] = holidayRepo.select(calendar, from.getOrElse(defaultFrom))

  def insertHoliday(holiday: Holiday): Future[Boolean] = holidayRepo.insert(holiday).map(_ => true)

  //add business logic to db query result
  def deleteAllHolidays: Future[Boolean] = holidayRepo.deleteAll.map(_ => true)

  def deleteHoliday(calendar: Calendar, date: LocalDate): Future[Boolean] = holidayRepo.delete(calendar, date).map(_ => true)

  def getNextBusinessDay(calendar: Calendar, date: LocalDate): Future[LocalDate] = {
    Future(findNextPrevBusinessDay(calendar, date, 1))
  }

  def getPreviousBusinessDay(calendar: Calendar, date: LocalDate): Future[LocalDate] = {
    Future(findNextPrevBusinessDay(calendar, date, -1))
  }

  def isTodayBusinessDay(calendar: Calendar): Future[Boolean] = {
    isHoliday(calendar, LocalDate.now(ZoneId.of("JST", ZoneId.SHORT_IDS))).map(x => !x)
  }

  def isBusinessDay(calendar: Calendar, date: LocalDate): Future[Boolean] = {
    isHoliday(calendar, date).map(x => !x)
  }

  def isHoliday(calendar: Calendar, date: LocalDate): Future[Boolean] = {
    holidayRepo.selectOne(calendar, date).map(result => result.length > 0 || isWeekend(date))
  }

  def isWeekend(date: LocalDate): Boolean = date.getDayOfWeek() == DayOfWeek.SUNDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY

  //to add .value method to future
  implicit class RichFuture[T](future: Future[T]) {
    def value: T = {
      Await.result(future)
    }
  }

}
