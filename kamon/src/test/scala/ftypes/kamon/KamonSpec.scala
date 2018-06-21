package ftypes.kamon

import cats.effect.IO
import cats.implicits._
import com.typesafe.config.Config
import kamon.metric._
import kamon.testkit.{MetricInspection, Reconfigure}
import kamon.{MetricReporter, Kamon => KamonMetrics}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.duration._

class KamonSpec extends FlatSpec with Matchers with Eventually with BeforeAndAfterEach with MetricInspection with Reconfigure {

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(1.second, 50.millis)

  val reporter = new MetricReporter {
    private var maybeSnapshot: Option[PeriodSnapshot] = None

    def metrics: MetricsSnapshot = maybeSnapshot.map(_.metrics).orNull

    override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit = {
      maybeSnapshot = Some(snapshot)
      ()
    }

    override def start(): Unit = ()
    override def stop(): Unit = ()
    override def reconfigure(config: Config): Unit = ()
  }

  override def beforeEach(): Unit = {
    enableFastMetricFlushing()
    enableFastSpanFlushing()
    enableSpanMetricScoping()
    sampleAlways()
    KamonMetrics.addReporter(reporter)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    KamonMetrics.stopAllReporters()
    super.afterEach()
  }

  val kamon = Kamon[IO]

  implicit val testTags: Map[String, String] = Map("foo" -> "bar")

  "#counter" should "add metrics" in {
    kamon.count("test.counter").unsafeRunSync()
    
    eventually {
      val metric = reporter.metrics.counters.last
      metric.name shouldBe "test.counter"
      metric.tags shouldBe testTags
    }
  }

  "#histogram" should "add metrics" in {
    kamon.histogram("test.histogram", 5L, MeasurementUnit.time.seconds).unsafeRunSync()

    eventually {
      val metric = reporter.metrics.histograms.last
      metric.name shouldBe "test.histogram"
      metric.tags shouldBe testTags
    }
  }

  "#sampler" should "add metrics" in {
    val test = for {
      _ <- kamon.sampler("test.sampler")(_.increment(100L))
      _ <- kamon.sampler("test.sampler")(_.decrement(10L))
    } yield ()
    test.unsafeRunSync()

    eventually {
      val metric = reporter.metrics.rangeSamplers.last
      metric.name shouldBe "test.sampler"
      metric.tags shouldBe testTags
      metric.distribution.max shouldBe 90L
      metric.distribution.min shouldBe 90L
    }
  }

  "#gauge" should "add metrics" in {
    val test = for {
      _ <- kamon.gauge("test.gauge")(_.increment(100L))
    } yield ()
    test.unsafeRunSync()

    eventually {
      val metric = reporter.metrics.gauges.last
      metric.name shouldBe "test.gauge"
      metric.tags shouldBe testTags
      metric.value shouldBe 100L
    }
  }

  "#timer" should "add metrics" in {
    val test = kamon.timer("test.time")(IO(Thread.sleep(10)))
    test.unsafeRunSync()

    eventually {
      val metric = reporter.metrics.histograms.last
      metric.name shouldBe "test.time"
      metric.tags shouldBe testTags
    }
  }

  "#trace" should "add metrics"
}
