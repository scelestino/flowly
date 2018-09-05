package flowly.task

/**
  *
  */
trait SimpleTask extends Task {

  def next: Task

  def followedBy: Seq[Task] = next :: Nil

}