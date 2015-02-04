import models.KairosDB
import actors.{StartFeeds, PulseParentActor}
import akka.actor.{Props, ActorRef}
import com.typesafe.config.ConfigFactory
import play.api.libs.concurrent.Akka
import play.api.{Logger, Application, GlobalSettings}

import scala.util.{Failure, Success}

/**
 * Pulse takes care of health management by checking on Sla's
 */
object Global extends GlobalSettings {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def onStart (application: Application): Unit = {

    import play.api.Play.current

    val conf = ConfigFactory.load()
    val zkConnect = conf.getString("kafka.zookeeper.connect")

    Logger.info("Starting Actor system")
    val pulseParent : ActorRef =  Akka.system.actorOf(Props[PulseParentActor], "pulseParent")

    Logger.info(s"Connecting to Zookeeper on $zkConnect")
    pulseParent ! StartFeeds(zkConnect = zkConnect)

    //Check KairosDB health
    val healthyKairosDB = KairosDB.Health

    healthyKairosDB.onComplete({
      case Success(returnCode) =>
        if(returnCode < 399)
        { Logger.info(s"Successfully connected to the Kairos DB on ${KairosDB.uri}")}
        else
        { Logger.error("KairosDB is running, but is not healthy")}
      case Failure(exception) => Logger.info(s"Error connecting to the KairosDB on ${KairosDB.uri}")

    })


  }
}