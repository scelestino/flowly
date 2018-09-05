package flowly.tasks

/**
  * An instance of this [[Task]] is followed with one Task.
  *
  */
trait SingleTask extends Task {

  def next: Task

  def followedBy: Seq[Task] = next :: Nil

}