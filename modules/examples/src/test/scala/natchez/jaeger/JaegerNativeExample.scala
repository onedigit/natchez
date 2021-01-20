// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package natchez.jaeger

import com.typesafe.scalalogging.StrictLogging
import io.jaegertracing.{internal, Configuration}
import io.jaegertracing.internal.samplers.ConstSampler
import io.jaegertracing.internal.{JaegerTracer, MDCScopeManager}
import io.jaegertracing.Configuration.{ReporterConfiguration, SamplerConfiguration, SenderConfiguration}

/**
  * Use jaeger client directly to test whether jaeger is working or not.
  *
  * @see https://github.com/opentracing-contrib/java-opentracing-walkthrough/blob/master/microdonuts/src/main/java/com/otsample/api/App.java
  */
object JaegerNativeExample extends StrictLogging {

  def main(args: Array[String]): Unit = {

    val tracer = configureTracer()

    val span: internal.JaegerSpan = tracer.buildSpan("hello").start()
    val scope                     = tracer.scopeManager().activate(span)
    logger.info("Starting..............")
    Thread.sleep(1000)
    logger.info("hello world")
    scope.close()
    span.finish()

    Thread.sleep(5000)
  }

  def configureTracer(): JaegerTracer = {
    val samplerConfiguration = new SamplerConfiguration()
      .withType(ConstSampler.TYPE)
      .withParam(1)

    val senderConfiguration = new SenderConfiguration()
      .withAgentHost("tr.onedigit.org")
      .withAgentPort(5775)
    // .withEndpoint("http://tr.onedigit.org:16686")
    val reporterConfiguration = ReporterConfiguration
      .fromEnv()
      .withLogSpans(true)
      .withFlushInterval(100)
      .withMaxQueueSize(1)
      .withSender(senderConfiguration)

    val configuration = new Configuration("jaeger-native-example")
      .withReporter(reporterConfiguration)
      .withSampler(samplerConfiguration)

    val scopeManager = new MDCScopeManager.Builder().build()
    configuration.getTracerBuilder
      .withScopeManager(scopeManager)
      .build()
  }
}
