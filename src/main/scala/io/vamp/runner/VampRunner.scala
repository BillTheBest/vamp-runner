package io.vamp.runner

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object VampRunner extends App with WebServer with Banner {

  implicit lazy val system = ActorSystem("vamp-runner")
  implicit lazy val materializer = ActorMaterializer()

  banner()

  val http = httpServe()

  sys.addShutdownHook {
    http.map(_.unbind()).onComplete(_ ⇒ system.terminate())
  }
}
