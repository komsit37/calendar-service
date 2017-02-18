package com.folio.calendar.controller

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import com.folio.calendar.idl.CalendarService
import com.folio.calendar.idl.CalendarService.{DeleteHoliday, GetHolidays, GetNextBusinessDay, GetPreviousBusinessDay, InsertHoliday, IsBusinessDay, IsHoliday, IsTodayBusinessDay}
import com.folio.calendar.model.Holiday
import com.folio.calendar.service.HolidayService
import com.twitter.finatra.thrift.Controller
import com.folio.calendar.idl

@Singleton
class CalendarController @Inject()(holidayService: HolidayService)
  extends Controller
    with CalendarService.BaseServiceIface {

  def serializeDate(ld: LocalDate): String = ld.format(DateTimeFormatter.ISO_LOCAL_DATE)
  def parseDate(ldStr: String): LocalDate = LocalDate.parse(ldStr, DateTimeFormatter.ISO_LOCAL_DATE)
  def fromIdl(d: idl.Holiday): Holiday = Holiday(d.calendar, parseDate(d.date), d.note)
  def toIdl(d: Holiday): idl.Holiday = idl.Holiday(d.calendar, serializeDate(d.date), d.note)

  // Since holidays are the inverse of business days, return the next non-holiday
  override val getNextBusinessDay = handle(GetNextBusinessDay) { args: GetNextBusinessDay.Args =>
    holidayService.getNextBusinessDay(args.calendar, parseDate(args.date)).map(x => serializeDate(x))
  }

  override val isTodayBusinessDay = handle(IsTodayBusinessDay) { args: IsTodayBusinessDay.Args =>
    holidayService.isTodayBusinessDay(args.calendar)
  }

  override val isBusinessDay = handle(IsBusinessDay) { args: IsBusinessDay.Args =>
    holidayService.isBusinessDay(args.calendar,  parseDate(args.date))
  }

  // Return true if the day is marked as holiday in db OR is a weekend
  override val isHoliday = handle(IsHoliday) { args: IsHoliday.Args =>
    holidayService.isHoliday(args.calendar,  parseDate(args.date))
  }

  override val insertHoliday = handle(InsertHoliday) { args: InsertHoliday.Args =>
    holidayService.insertHoliday(fromIdl(args.holiday))
  }

  override val getHolidays = handle(GetHolidays) { args: GetHolidays.Args =>
    holidayService.getHolidays(args.calendar, args.fromDate.map(parseDate)).map(_.map(x => toIdl(x)))
  }

  override def getPreviousBusinessDay = handle(GetPreviousBusinessDay) { args: GetPreviousBusinessDay.Args =>
    holidayService.getPreviousBusinessDay(args.calendar, parseDate(args.date)).map(x => serializeDate(x))
  }
  override def deleteHoliday = handle(DeleteHoliday) { args: DeleteHoliday.Args =>
    holidayService.deleteHoliday(args.calendar,  parseDate(args.date))
  }
}
