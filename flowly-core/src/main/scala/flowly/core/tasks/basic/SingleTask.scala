package flowly.core.tasks.basic

/**
  * An instance of this [[Task]] is followed by one [[Task]].
  *
  */
trait SingleTask extends Task {

  val next: Task

  private[flowly] def followedBy: List[Task] = next :: Nil

}
