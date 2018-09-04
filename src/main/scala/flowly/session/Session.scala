package flowly.session

import java.time.LocalDateTime

case class Session(sessionId: String, lastExecution: Option[Execution], variables: Map[String, Any], createdAt: LocalDateTime)

case class Execution(taskId: String, status: String, at: LocalDateTime, msg: Option[String] = None)
