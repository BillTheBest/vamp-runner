package io.vamp.runner

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object VampRunner extends App with VampRecipes {

  val logger = Logger(LoggerFactory.getLogger(VampRunner.getClass))

  logger.info(
    s"""
       |██╗   ██╗ █████╗ ███╗   ███╗██████╗
       |██║   ██║██╔══██╗████╗ ████║██╔══██╗
       |██║   ██║███████║██╔████╔██║██████╔╝
       |╚██╗ ██╔╝██╔══██║██║╚██╔╝██║██╔═══╝
       | ╚████╔╝ ██║  ██║██║ ╚═╝ ██║██║
       |  ╚═══╝  ╚═╝  ╚═╝╚═╝     ╚═╝╚═╝
       |                       runner
       |                       by magnetic.io
    """.stripMargin)

  val url = ConfigFactory.load().getString("vamp.runner.url")

  logger.info(s"Vamp API URL: ${VampApi.url}")

  implicit val actorSystem = ActorSystem("vamp-runner")
  implicit val executionContext = actorSystem.dispatcher

  Http(actorSystem)

  val runnables = if (args.isEmpty) recipes
  else recipes.filter {
    case (name, _) ⇒
      val contains = args.contains(name)
      if (!contains) throw new RuntimeException(s"No recipe: $name")
      contains
  }

  logger.info("Running recipes...")

  Future.sequence {
    runnables.map {
      case (name, recipe) ⇒
        logger.info(s"Running recipe: $name")
        recipe.run
    }
  } recover {
    case failure ⇒ logger.error(s"Failure: ${failure.getMessage}")
  } onComplete {
    case _ ⇒
      logger.info("Done.")
      Http().shutdownAllConnectionPools()
      actorSystem.terminate()
  }
}