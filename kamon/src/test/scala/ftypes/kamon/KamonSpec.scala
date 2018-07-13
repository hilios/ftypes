package ftypes.kamon

import java.time.Instant

import cats.effect.IO
import cats.implicits._
import com.typesafe.config.Config
import kamon.metric._
import kamon.testkit.{MetricInspection, Reconfigure}
import kamon.{MetricReporter, Kamon => KamonMetrics}
import org.scalatest.concurrent.Eventually
import org.scalatest.fixture.FlatSpec
import org.scalatest.{BeforeAndAfterEach, Matchers, Outcome}

import scala.concurrent.duration._

class KamonSpec extends FlatSpec with Matchers with Eventually with BeforeAndAfterEach with MetricInspection with Reconfigure {

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(1.second, 50.millis)

  val kamon = Kamon[IO]

  final class FixtureParam extends MetricReporter {
    private val accumulator = new PeriodSnapshotAccumulator(
      java.time.Duration.ofSeconds(60),
      java.time.Duration.ofSeconds(1)
    )

    def metrics: MetricsSnapshot = accumulator.peek().metrics

    override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit = {
      accumulator.add(snapshot)
      ()
    }

    override def start(): Unit = ()
    override def stop(): Unit = ()
    override def reconfigure(config: Config): Unit = ()
  }

  override def withFixture(test: OneArgTest): Outcome = {
    val reporter = new FixtureParam
    KamonMetrics.addReporter(reporter)

    try withFixture(test.toNoArgTest(reporter))
    finally reporter.stop()
  }

  override def beforeEach(): Unit = {
    enableFastMetricFlushing()
    enableFastSpanFlushing()
    enableSpanMetricScoping()
    sampleAlways()
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    KamonMetrics.stopAllReporters()
    super.afterEach()
  }

  implicit val testTags: Map[String, String] = Map("foo" -> "bar")

  "#counter" should "add metrics" in { reporter =>
    kamon.count("test.counter").unsafeRunSync()
    
    eventually {
      val metric = reporter.metrics.counters.last
      metric.name shouldBe "test.counter"
      metric.tags shouldBe testTags
      metric.value shouldBe 1L
    }
  }

  "#histogram" should "add metrics" in { reporter =>
    kamon.histogram("test.histogram", 5L, MeasurementUnit.time.seconds).unsafeRunSync()

    eventually {
      val metric = reporter.metrics.histograms.last
      metric.name shouldBe "test.histogram"
      metric.tags shouldBe testTags
    }
  }

  "#rangeSampler" should "add metrics" in { reporter =>
    val test = for {
      _ <- kamon.rangeSampler("test.sampler")(_.increment(100L))
      _ <- kamon.rangeSampler("test.sampler")(_.decrement(10L))
    } yield ()
    test.unsafeRunSync()

    eventually {
      val metric = reporter.metrics.rangeSamplers.head
      metric.name shouldBe "test.sampler"
      metric.tags shouldBe testTags
//      metric.distribution.sum shouldBe 190L
//      metric.distribution.max shouldBe 100L
    }
  }

  "#gauge" should "add metrics" in { reporter =>
    val test = for {
      _ <- kamon.gauge("test.gauge")(_.increment(100L))
      _ <- kamon.gauge("test.gauge")(_.decrement(10L))
    } yield ()
    test.unsafeRunSync()

    eventually {
      val metric = reporter.metrics.gauges.last
      metric.name shouldBe "test.gauge"
      metric.tags shouldBe testTags
      metric.value shouldBe 90L
    }
  }

  "#timer" should "add metrics" in { reporter =>
    val test = kamon.timer("test.time")(IO(Thread.sleep(100)))
    test.unsafeRunSync()

    eventually {
      val metric = reporter.metrics.histograms.last
      metric.name shouldBe "test.time"
      metric.tags shouldBe testTags
      metric.distribution.count shouldBe 1L
      metric.distribution.sum / 1000000 shouldBe 100L +- 10L
    }
  }

  "#trace" should "not fail" in { reporter =>
    val test = kamon.trace("test.trace") { s =>
      for {
        _ <- s.tag("foo", "bar")
        _ <- s.mark("zoo")
        _ <- s.addError("Boom!", new Exception("Boom!"))
        _ <- s.tagMetric("foo", "bar")
        _ <- s.setOperationName("name")
        _ <- s.enableMetrics()
        _ <- s.disableMetrics()
        _ <- s.nonEmpty()
        _ <- IO(Thread.sleep(10))
        _ <- s.finish(Instant.now())
      } yield ()
    }
    test.unsafeRunSync()
    reporter.metrics
  }
}

