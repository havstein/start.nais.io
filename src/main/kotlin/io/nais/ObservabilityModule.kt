package io.nais

import io.ktor.application.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

@Suppress("unused") // referenced in application.conf
fun Application.observability() {
   val collectorRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

   install(MicrometerMetrics) {
      registry = collectorRegistry

      meterBinders = listOf(
         ClassLoaderMetrics(),
         JvmMemoryMetrics(),
         JvmGcMetrics(),
         ProcessorMetrics(),
         JvmThreadMetrics()
      )
   }

   routing {
      get("/internal/isalive") {
         call.respond(OK)
      }

      get("/internal/isready") {
         call.respond(OK)
      }

      get("/internal/metrics") {
         val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
         call.respond(collectorRegistry.scrape())
      }
   }
}
