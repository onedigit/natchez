// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package example

import cats.{~>, Applicative}
import cats.data._
import cats.effect.{BracketThrow, ExitCode, IO, IOApp, Resource}
import natchez._

// noinspection DuplicatedCode
object ExampleTwo extends IOApp {

  private final val NUM_VALUES_TO_SORT = 20

  override def run(args: List[String]): IO[ExitCode] = {

    val ff = runF[Kleisli[IO, Span[IO], *]]()

    val result = Example
      .entryPoint[IO]
      .use(ep =>
        ep.root("example2")
          .use(spanF => ff(spanF))
      )

    result as ExitCode.Success
  }

  def runF[F[_]: Applicative: Trace](): F[Int] = {
    Trace[F].span("f()") {
      Applicative[F].pure(42)
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  def typeFoo[F[_]: BracketThrow](entryPoint: EntryPoint[F], kernel: Kernel): Unit = {

    type G[A] = Kleisli[F, Span[F], A] // Span[F] => F[A]

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

  // https://github.com/tpolecat/skunk/blob/master/modules/example/src/main/scala-2/Http4s.scala
  // https://github.com/tpolecat/skunk/blob/master/modules/example/src/main/scala-2/NatchezHttp4sModule.scala

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

  // -------------------------------------------------------------------------------------------------------------------

}
