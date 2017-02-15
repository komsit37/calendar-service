namespace java com.folio.calendar.idl
#@namespace scala com.folio.calendar.idl
namespace rb Calendar

include "finatra-thrift/finatra_thrift_exceptions.thrift"

typedef string LocalDate

enum Calendar {
    JPX
    Japannext
    NASDAQ
}

struct Holiday {
  1: Calendar calendar
  2: LocalDate date
  3: optional string note
}


// business day = non-holiday, non-weekend
// holiday = weekend + any holiday
service CalendarService {

    //return first business day since start date (inclusive)
    //example if 2017-02-17 (Friday) is a holiday,
    // calling getNextBusinessDay on 2017-02-16 will return 2017-02-16 (Thursday) (already a business day)
    // calling getNextBusinessDay on 2017-02-17 will return 2017-02-20 (Monday) because 17 is a holiday and 18-19 is weekend
    LocalDate getNextBusinessDay(
        1: Calendar calendar
        2: LocalDate date
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )

    //return first business day before start date (inclusive)
    //example if 2017-02-17 (Friday) is a holiday,
    // calling getPreviousBusinessDay on 2017-02-20 will return 2017-02-20 (Monday) (already a business day)
    // calling getPreviousBusinessDay on 2017-02-19 will return 2017-02-16 (Thursday) because 18-19 is weekend and 17 is a holiday
    LocalDate getPreviousBusinessDay(
        1: Calendar calendar
        2: LocalDate date
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )

    //current date in JST
    bool isTodayBusinessDay(
        1: Calendar calendar
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )

    bool isBusinessDay(
        1: Calendar calendar
        2: LocalDate date
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )

    /**
    * Returns true if a day is a holiday.
    */
    bool isHoliday(
        1: Calendar calendar
        2: LocalDate date
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )

    /**
    * Get holidays of a given calendar between two dates.
    */
    list<Holiday> getHolidays(
        1: Calendar calendar
        2: LocalDate fromDate
        3: LocalDate toDate
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )


    //management API
    bool insertHoliday(
        1: Holiday holiday
    ) throws (
        1: finatra_thrift_exceptions.ServerError serverError
    )

    bool deleteHoliday(
      1: Calendar calendar
      2: LocalDate date
    ) throws (
        1: finatra_thrift_exceptions.ServerError  serverError
    )
}
