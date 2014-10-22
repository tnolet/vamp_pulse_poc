package actors

import akka.actor.{Actor, ActorLogging, Props}
import com.sclasen.akka.kafka.{AkkaConsumer, AkkaConsumerProps, CommitConfig}
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.I0Itec.zkclient.exception.ZkTimeoutException
import play.api.Logger

import scala.concurrent.duration._
/**
 * PulseParentActor functions as the parent to all actors communicating with Kafka and other feeds suppliers
 */

trait PulseMessage
case class StartFeeds(zkConnect: String) extends PulseMessage

class PulseParentActor extends Actor with ActorLogging {

  override def preStart() = {

    Logger.info("Creating Score Board")
    context.actorOf(Props[ScoreBoardActor], "scoreBoard")

    Logger.info("Creating Metrics DB Feeder")
    context.actorOf(Props[MetricsDBActor], "metricsDbFeeder")

  }

  def receive = {

    case s: StartFeeds =>


      log.info("Starting Kafka feeds")

      val lbMetricsFeed = context.actorOf(Props[LoadBalancerTopicActor], "lbMetricsFeed")

      val lbMetricsFeedCommitConfig = CommitConfig(
        commitInterval = Some(10 seconds),
        commitAfterMsgCount = Some(60),
        commitTimeout = 5 seconds
      )

      val consumerProps = AkkaConsumerProps.forContext(
        context = context,
        zkConnect = s.zkConnect,
        topic = "loadbalancer.all",
        group = "vamp-pulse-consumer",
        streams = 1,
        keyDecoder = new DefaultDecoder(),
        msgDecoder = new StringDecoder(),
        receiver = lbMetricsFeed,
        commitConfig = lbMetricsFeedCommitConfig
      )



      val lbFeedsConsumer = new AkkaConsumer(consumerProps)
      try {
        lbFeedsConsumer.start()

      } catch {

        case zkt: ZkTimeoutException => log.error("Connecting to Zookeeper for feeds failed")
      }

    }
}
