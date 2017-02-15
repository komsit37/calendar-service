package com.folio.calendar.module

import javax.inject.Singleton

import com.google.inject.Provides
import com.twitter.inject.TwitterModule
import io.getquill.{FinagleMysqlContext, Literal}

object QuillDbContextModule extends TwitterModule {

  @Provides
  @Singleton
  def provideQuillDbContext: FinagleMysqlContext[Literal] = {
    new FinagleMysqlContext[Literal]("ctx")
  }

}
