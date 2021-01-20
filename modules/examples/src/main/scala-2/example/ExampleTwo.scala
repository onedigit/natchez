// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package example

import cats.effect.IO
import natchez.{Span, Trace}

import natchez.Trace.Implicits._

object ExampleTwo {

  def main(args: Array[String]): Unit = {
    // val span: Span[IO] = ???
    val r1 = f().unsafeRunSync()
    println(s"r1: $r1")
  }

  def f(): IO[Int] = {
    Trace[IO].span("f()") {
      IO.pure(42)
    }
  }
}
