import actors.{StartFeeds, PulseParentActor}
import akka.actor.{Props, ActorRef}
import com.typesafe.config.ConfigFactory
import play.api.libs.concurrent.Akka
import play.api.{Logger, Application, GlobalSettings}

/**
 * Pulse takes care of health management by checking on Sla's
 */
object Global extends GlobalSettings {

  override def onStart (application: Application): Unit = {

    import play.api.Play.current

    val conf = ConfigFactory.load()
    val zkConnect = conf.getString("kafka.zookeeper.connect")

    Logger.info("Starting Actor system")
    val pulseParent : ActorRef =  Akka.system.actorOf(Props[PulseParentActor], "pulseParent")

    Logger.info(s"Connecting to Zookeeper on $zkConnect")
    pulseParent ! StartFeeds(zkConnect = zkConnect)


  }
}