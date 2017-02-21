package com.folio.calendar.service

import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate, ZoneId}
import javax.inject.Inject

import com.folio.calendar.idl.Calendar
import com.folio.calendar.model.{Holiday, HolidayRepo}
import com.twitter.util.Future

@Inject
class HolidayService @Inject()(holidayRepo: HolidayRepo) {
  def findNextPrevBusinessDay(calendar: Calendar, date: LocalDate, inc: Int): Future[LocalDate]  = {
    isBusinessDay(calendar, date).flatMap(b => {
       if(b) Future(date)
       else findNextPrevBusinessDay(calendar, date.plusDays(inc), inc)
    })
  }

  //default from beginning of this year till end of next year
  val BeginningOfThisYear = LocalDate.now.`with`(TemporalAdjusters.firstDayOfYear())
  val EndOfNextYear = LocalDate.now.plusYears(1)`with`(TemporalAdjusters.lastDayOfYear())

  def getHolidays(calendar: Calendar, from: Option[LocalDate] = None, to: Option[LocalDate] = None): Future[Seq[Holiday]]
  = holidayRepo.select(calendar, from.getOrElse(BeginningOfThisYear), to.getOrElse(EndOfNextYear))

  def insertHoliday(holiday: Holiday): Future[Boolean] = holidayRepo.insert(holiday).map(_ => true)

  //add business logic to db query result
  def deleteAllHolidays: Future[Boolean] = holidayRepo.deleteAll.map(_ => true)

  def deleteHoliday(calendar: Calendar, date: LocalDate): Future[Boolean]
  = holidayRepo.delete(calendar, date).map(numDeleted => numDeleted > 0)

  def getNextBusinessDay(calendar: Calendar, date: LocalDate): Future[LocalDate] = {
    findNextPrevBusinessDay(calendar, date, 1)
  }

  def getPreviousBusinessDay(calendar: Calendar, date: LocalDate): Future[LocalDate] = {
    findNextPrevBusinessDay(calendar, date, -1)
  }

  def isTodayBusinessDay(calendar: Calendar): Future[Boolean] = {
    isHoliday(calendar, LocalDate.now(ZoneId.of("JST", ZoneId.SHORT_IDS))).map(x => !x)
  }

  def isBusinessDay(calendar: Calendar, date: LocalDate): Future[Boolean] = {
    isHoliday(calendar, date).map(x => !x)
  }

  def isHoliday(calendar: Calendar, date: LocalDate): Future[Boolean] = {
    holidayRepo.selectOne(calendar, date).map(result => result.nonEmpty || isWeekend(date))
  }

  def isWeekend(date: LocalDate): Boolean = date.getDayOfWeek() == DayOfWeek.SUNDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY

}
