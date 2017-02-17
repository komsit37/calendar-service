# Calendar Server

Mainly taken from https://github.com/twitter/finatra/tree/develop/examples/thrift-server while adding quill db example code
## Prerequisite
you'll need flyway command-line https://flywaydb.org/documentation/commandline/
```bash
C:\Users\pkomsit.FOLIO-SEC\dev\scala\folio\finatra-quill-template>flyway -v
Flyway 4.0.3 by Boxfuse
```

## Setup
update db config in (current setup is to connect to mysql using user root with no password)
* flyway.conf
* calendar-server/src/main/resources/application.properties

then run below from command line
```bash
> flyway migrate //to create initial db schema
> sbt compile
```
to compile thrift idl and generate scala client code
```
sbt calendar-idl/compile
```
client code will be generated to calendar-idl/target/scala-2.11/src_managed

to run server
```
sbt "calendar-server/run"
```

to run server with auto reload when files are changed
```
sbt "~calendar-server/re-start"
```

You can see sample data on http://localhost:9910/holidays or run ThriftClientExample (below) to call thrift API

to run test client
```
sbt "calendar-server/test:runMain com.folio.calendar.example.ThriftClientExample"
```

to run unit test
```
> sbt test
```

## Dependencies
* flyway - database versioning and migration
* finatra - framework for thrift server
  * guice - library for dependency injection
  * finagle - underlying RPC library used by finatra
* quill - library to generate sql from case class at compile time
* finagle-mysql - asynchronous mysql database connection library (kind of like jdbc, but it's asynchronous)
* scrooge-sbt-plugin - sbt plugin to generate scala code from thrift

## Useful Docs
* finatra/finagle
  * https://github.com/twitter/finatra
  * https://github.com/twitter/finatra/tree/develop/examples/thrift-server
  * Futures in https://twitter.github.io/scala_school/finagle.html
  * https://twitter.github.io/finagle/guide/Quickstart.html
  * scrooge https://twitter.github.io/scrooge/SBTPlugin.html
* guice https://github.com/google/guice/wiki/Motivation
* scalatest
  * http://www.scalatest.org/user_guide/selecting_a_style we mainly use wordSpec style
  * http://www.scalatest.org/user_guide/using_matchers
* quill
  * http://getquill.io/
  * https://github.com/getquill/quill/tree/master/quill-finagle-mysql/src/test
* sbt http://www.scala-sbt.org/0.13/docs/Multi-Project.html
* flyway https://flywaydb.org/documentation/commandline/