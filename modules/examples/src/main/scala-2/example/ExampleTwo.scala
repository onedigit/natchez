// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package example

import cats.data.{Kleisli, OptionT}
import cats.effect.{Bracket, BracketThrow, IO, Resource}
import cats.{~>, Functor}
import natchez.{EntryPoint, Kernel, Span, Trace}
import natchez.Trace.Implicits._

object ExampleTwo {

  def main(args: Array[String]): Unit = {
    // val span: Span[IO] = ???
    val r1 = f().unsafeRunSync()
  }

  def f(): IO[Int] = {
    Trace[IO].span("f()") {
      IO.pure(42)
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  def typeFoo[F[_]: BracketThrow](entryPoint: EntryPoint[F], kernel: Kernel): Unit = {

    type G[A] = Kleisli[F, Span[F], A] // Span[F] => Span[F[A]]

    val lift: F ~> G = λ[F ~> G](fa => Kleisli((sf: Span[F]) => fa))

    val spanR: Resource[F, Span[F]] = entryPoint.continueOrElseRoot("hello", kernel)

    OptionT {
      spanR.use { span =>
        val lower = λ[G ~> F](k => k(span)) // opposite of lift?
        ???
      }
      ???
    }

    // val k1: Kleisli[List, Span[List], Int] = Kleisli.liftF(List(1, 2, 3))
  }

  // https://gitter.im/ovotech/general?at=5de7d1b1b065c6433c389312
  //
  //
  //      type G[A]  = Kleisli[F, Span[F], A]
  //      val lift   = λ[F ~> G](fa => Kleisli(_ => fa))
  //      val kernel = Kernel(req.headers.toList.map(h => (h.name.value -> h.value)).toMap)
  //      val spanR  = entryPoint.continueOrElseRoot(req.uri.path, kernel)
  //      OptionT {
  //        spanR.use { span =>
  //          val lower = λ[G ~> F](_(span))
  //          routes.run(req.mapK(lift)).mapK(lower).map(_.mapK(lower)).value
  //        }
  //      }
}
