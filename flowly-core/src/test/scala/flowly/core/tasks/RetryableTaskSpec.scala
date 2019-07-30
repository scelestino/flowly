package flowly.core.tasks

import java.time.Instant

import flowly.core.{BooleanKey, ErrorOr, TasksContext}
import flowly.core.tasks.basic.{ExecutionTask, FinishTask, Task}
import flowly.core.tasks.compose.RetryableTask
import flowly.core.tasks.model.{Continue, OnError, TaskAttempts, ToRetry}
import flowly.core.tasks.strategies.scheduling.SchedulingStrategy
import flowly.core.tasks.strategies.stopretrying.StopRetryingStrategy
import flowly.core.variables.{ExecutionContext, ReadableExecutionContext}
import org.specs2.mutable.{Before, Specification}
import org.specs2.mock.Mockito

class RetryableTaskSpec extends Specification with Mockito with Before {

  private var ss: SchedulingStrategy = _

  private var srs: StopRetryingStrategy = _

  class RetryableExecutionTask extends ExecutionTask() with RetryableTask {

     override val id: String = "1"

     override protected def perform(sessionId: String, executionContext: ExecutionContext): ErrorOr[ExecutionContext] = {
       executionContext.getOrError(BooleanKey).map(_ => executionContext)
     }

     override protected def schedulingStrategy: SchedulingStrategy = ss

     override protected def stopRetryingStrategy: StopRetryingStrategy = srs

     override val next: Task = FinishTask("2")
   }

  override def before: Unit = {
    ss = mock[SchedulingStrategy]
    srs = mock[StopRetryingStrategy]
    ss.nextRetryDate(any[TaskAttempts](), any[ReadableExecutionContext]()) returns Instant.now()
  }

  "RetryableTask" should {

    "return ToRetry when perform with errors and stop condition not reached" in new TasksContext {
      srs.shouldRetry(any[TaskAttempts](), any[ReadableExecutionContext]()) returns true
      val retryableTask = new RetryableExecutionTask
      retryableTask.execute("session1", ec) must haveClass[ToRetry]
    }

    "return OnError when perform with errors and stop condition reach" in new TasksContext {
      srs.shouldRetry(any[TaskAttempts](), any[ReadableExecutionContext]()) returns false
      val retryableTask = new RetryableExecutionTask
      retryableTask.execute("session1", ec) must haveClass[OnError]
    }

    "return Continue when perform Ok" in new TasksContext {
      srs.shouldRetry(any[TaskAttempts](), any[ReadableExecutionContext]()) returns false
      val retryableTask = new RetryableExecutionTask
      retryableTask.execute("session1", ec.set(BooleanKey, true)) must haveClass[Continue]
    }

  }

}