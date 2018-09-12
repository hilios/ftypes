package ftypes.kamon

import java.util.concurrent.{ExecutorService, ForkJoinPool}

import kamon.executors.Executors.{ForkJoinPoolMetrics, InstrumentedExecutorService}
import kamon.executors.util.ContextAwareExecutorService

object KamonExecutor {

  def apply: ExecutorService = instrument(new ForkJoinPool())

  /**
    * Decorates passed fork-join pool to support kamon context and metrics
    *
    * @param pool source fork join thread pool
    * @return instrumented pool
    */
  def instrument(pool: ForkJoinPool): ExecutorService = {

    // type class instance for the ContextAwareExecutorService]]
    val metrics = new ForkJoinPoolMetrics[ContextAwareExecutorService] {
      override def minThreads(p: ContextAwareExecutorService): Int    = 0
      override def maxThreads(p: ContextAwareExecutorService): Int    = pool.getParallelism
      override def activeThreads(p: ContextAwareExecutorService): Int = pool.getActiveThreadCount
      override def poolSize(p: ContextAwareExecutorService): Int      = pool.getPoolSize
      override def queuedTasks(p: ContextAwareExecutorService): Int   = pool.getQueuedSubmissionCount
      override def parallelism(p: ContextAwareExecutorService): Int   = pool.getParallelism
    }

    // instrument executor service using type class instance
    new InstrumentedExecutorService(ContextAwareExecutorService(pool))(metrics)
  }
}
