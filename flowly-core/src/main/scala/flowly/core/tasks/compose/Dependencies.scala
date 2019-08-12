package flowly.core.tasks.compose

trait Dependencies {

  /**
    * This method give us a compile-time check about [[Alternative]] use
    */
  private[flowly] def alternativeAfterAll():Unit = ()

}
