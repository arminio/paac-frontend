Frontend of the PAAC(Pensions Annual Allowance Calculator) application
======================================================================
[![Build Status](https://travis-ci.org/hmrc/paac-frontend.svg)](https://travis-ci.org/hmrc/paac-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/paac-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/paac-frontend/_latestVersion)

This service provides the frontend endpoint for the [Pension Annual Allowance Calculator](https://github.com/hmrc/paac).

Requirements
------------
* This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE](https://www.java.com/en/download/) to run.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


## Run the application

To run the application execute

```
sbt run
```

and then access the application at

```
http://localhost:9000/paac
```


## SBT Testing

Turn on full stacktrace in sbt console using `set testOptions in "paac-frontend" += Tests.Argument("-oF")`