// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package natchez
package jaeger

import cats.data.Nested
import cats.effect.{Resource, Sync}
import cats.syntax.all._
import io.opentracing
import io.opentracing.propagation._

import java.net.URI
import scala.jdk.CollectionConverters._

private[jaeger] final case class JaegerSpan[F[_]: Sync](
    tracer: opentracing.Tracer,
    span: opentracing.Span,
    prefix: Option[URI]
) extends Span[F] {

  import TraceValue._

  def kernel: F[Kernel] = {
    Sync[F].delay {
      val m = new java.util.HashMap[String, String]
      tracer.inject(
        span.context,
        Format.Builtin.HTTP_HEADERS,
        new TextMapAdapter(m)
      )
      Kernel(m.asScala.toMap)
    }
  }

  def put(fields: (String, TraceValue)*): F[Unit] =
    fields.toList.traverse_ {
      case (k, StringValue(v))  => Sync[F].delay(span.setTag(k, v))
      case (k, NumberValue(v))  => Sync[F].delay(span.setTag(k, v))
      case (k, BooleanValue(v)) => Sync[F].delay(span.setTag(k, v))
    }

  def span(name: String): Resource[F, Span[F]] = {
    val r: Resource[F, opentracing.Span] = Resource
      .make(
        Sync[F].delay(tracer.buildSpan(name).asChildOf(span).start)
      )(s => Sync[F].delay(s.finish()))
    r.map(span => JaegerSpan(tracer, span, prefix))
  }

  def traceId: F[Option[String]] = {
    // this seems to work … is it legit?
    kernel.map(
      _.toHeaders
        .get("uber-trace-id")
        .map(_.takeWhile(_ != ':'))
    )
  }

  def traceUri: F[Option[URI]] = {
    (
      Nested(prefix.pure[F]),
      Nested(traceId)
    ).mapN { (uri, id) =>
      uri.resolve(s"/trace/$id")
    }.value
  }

}
