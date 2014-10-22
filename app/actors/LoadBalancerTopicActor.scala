package actors

import akka.actor.{Actor, ActorLogging}
import com.sclasen.akka.kafka.StreamFSM
import models.DataPoint
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json

class LoadBalancerTopicActor extends Actor with ActorLogging {

  import play.api.Play.current

  val scoreBoard = Akka.system.actorSelection("akka://application/user/pulseParent/scoreBoard")
  val metricsDbFeeder = Akka.system.actorSelection("akka://application/user/pulseParent/metricsDbFeeder")


  def receive = {

    case data: String =>

      import models.DataPointJson.datapointReads

      val payload = Json.parse(data)

      // if the datapoint is valid. Send it to other handlers

      payload.validate[DataPoint].fold(
        valid = {
          datapoint => {


            //filter out all data from dummy and test services, only allow object starting with "vrn-"

            if ( datapoint.name.startsWith("vrn-") && !datapoint.name.contains("dummy")) {

              scoreBoard ! datapoint
              metricsDbFeeder ! datapoint
            }
          }
        },
        invalid = {
          errors => log.error(s"Error parsing metric: " + errors)
        }
      )

      sender ! StreamFSM.Processed

  }

}



